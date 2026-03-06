import http from "k6/http";
import { check, sleep } from "k6";
import { Trend } from "k6/metrics";

const baseUrl = __ENV.BASE_URL || "http://localhost:18080";
const seedUsers = Number(__ENV.SEED_USERS || 200);
const loginVus = Number(__ENV.LOGIN_VUS || 80);
const stableRate = Number(__ENV.STABLE_RATE || 60);
const overloadRate = Number(__ENV.OVERLOAD_RATE || 140);
const onlyHeavy = String(__ENV.ONLY_HEAVY || "0") === "1";
const disableThresholds = String(__ENV.K6_DISABLE_THRESHOLDS || "0") === "1";
const heavyProfileRps = (__ENV.HEAVY_PROFILE_RPS || "100,300,600,900,1200")
  .split(",")
  .map((x) => Number(String(x).trim()))
  .filter((x) => Number.isFinite(x) && x > 0);
const heavyStageDuration = __ENV.HEAVY_STAGE_DURATION || "90s";
const heavyFinalDrainDuration = __ENV.HEAVY_FINAL_DRAIN_DURATION || "120s";
const heavyTransitionDuration = __ENV.HEAVY_TRANSITION_DURATION || "1s";

const loginLatencyMs = new Trend("login_latency_ms");
const ratingLatencyMs = new Trend("rating_latency_ms");
const writeLatencyMs = new Trend("write_latency_ms");

function buildHeavyStages() {
  const stages = [];
  for (let i = 0; i < heavyProfileRps.length; i += 1) {
    const r = heavyProfileRps[i];
    if (i > 0) {
      stages.push({ target: r, duration: heavyTransitionDuration });
    }
    stages.push({ target: r, duration: heavyStageDuration });
  }
  stages.push({ target: 0, duration: heavyTransitionDuration });
  stages.push({ target: 0, duration: heavyFinalDrainDuration });
  return stages;
}

export const options = {
  discardResponseBodies: false,
  summaryTrendStats: ["min", "med", "max", "avg", "p(50)", "p(75)", "p(90)", "p(95)", "p(99)"],
  scenarios: onlyHeavy ? {
    heavy_overload_recovery: {
      executor: "ramping-arrival-rate",
      startRate: heavyProfileRps[0] || 100,
      timeUnit: "1s",
      preAllocatedVUs: 100,
      maxVUs: 3000,
      exec: "heavyWriteScenario",
      gracefulStop: "30m",
      stages: buildHeavyStages(),
    },
  } : {
    login_burst: {
      executor: "constant-vus",
      vus: loginVus,
      duration: "2m",
      exec: "loginBurstScenario",
      startTime: "0s",
    },
    rating_stable_load: {
      executor: "constant-arrival-rate",
      rate: stableRate,
      timeUnit: "1s",
      duration: "4m",
      preAllocatedVUs: 60,
      maxVUs: 120,
      exec: "ratingReadScenario",
      startTime: "2m",
    },
    heavy_overload_recovery: {
      executor: "ramping-arrival-rate",
      startRate: heavyProfileRps[0] || 100,
      timeUnit: "1s",
      preAllocatedVUs: 100,
      maxVUs: 3000,
      exec: "heavyWriteScenario",
      startTime: "6m",
      gracefulStop: "30m",
      stages: buildHeavyStages(),
    },
  },
  thresholds: disableThresholds ? {} : {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<3000"],
    login_latency_ms: ["p(95)<3000"],
    rating_latency_ms: ["p(95)<2500"],
    write_latency_ms: ["p(95)<3500"],
  },
};

function registerUser(username, password) {
  const email = `${username}@bench.local`;
  const payload = JSON.stringify({ username, email, password });
  return http.post(`${baseUrl}/api/v1/users`, payload, {
    headers: { "Content-Type": "application/json" },
    tags: { endpoint: "register_user" },
  });
}

function loginUser(username, password) {
  const payload = JSON.stringify({ username, password });
  return http.post(`${baseUrl}/api/v1/tokens`, payload, {
    headers: { "Content-Type": "application/json" },
    tags: { endpoint: "login_user" },
  });
}

function getLocationGroupId() {
  const groupName = "bench-default-group";
  const createResp = http.post(
    `${baseUrl}/api/v1/location-groups`,
    JSON.stringify({ name: groupName }),
    {
      headers: { "Content-Type": "application/json" },
      tags: { endpoint: "create_location_group" },
    }
  );

  if (![201, 409].includes(createResp.status)) {
    const listResp = http.get(`${baseUrl}/api/v1/location-groups`, {
      tags: { endpoint: "list_location_groups" },
    });
    if (listResp.status !== 200) {
      throw new Error(`Cannot resolve location group, status=${listResp.status}`);
    }
    const groups = JSON.parse(listResp.body);
    const found = groups.find((g) => g.name === groupName);
    if (!found) {
      throw new Error("Location group not found");
    }
    return found.id;
  }

  if (createResp.status === 201) {
    return JSON.parse(createResp.body).id;
  }

  const existingResp = http.get(`${baseUrl}/api/v1/location-groups/${groupName}`, {
    tags: { endpoint: "get_location_group" },
  });
  if (existingResp.status !== 200) {
    throw new Error(`Cannot fetch existing group, status=${existingResp.status}`);
  }
  return JSON.parse(existingResp.body).id;
}

function seedLocations(groupId) {
  const ids = [];
  for (let i = 0; i < 20; i += 1) {
    const lat = 50 + i * 0.01;
    const lng = 30 + i * 0.01;
    const resp = http.post(
      `${baseUrl}/api/v1/locations`,
      JSON.stringify({ lat, lng, groupId }),
      {
        headers: { "Content-Type": "application/json" },
        tags: { endpoint: "seed_location" },
      }
    );
    if (resp.status === 201) {
      ids.push(JSON.parse(resp.body).id);
    }
  }
  const listResp = http.get(`${baseUrl}/api/v1/locations/group/${groupId}`, {
    tags: { endpoint: "list_locations_group" },
  });
  if (listResp.status === 200) {
    return JSON.parse(listResp.body).map((x) => x.id);
  }
  return ids;
}

export function setup() {
  const warmupUsername = "bench-main";
  const warmupPassword = "bench-main-pass";
  const maybeUser = registerUser(warmupUsername, warmupPassword);
  check(maybeUser, {
    "warmup user registered": (r) => [201, 409].includes(r.status),
  });

  const tokenResp = loginUser(warmupUsername, warmupPassword);
  check(tokenResp, {
    "warmup login ok": (r) => r.status === 200,
  });
  const token = JSON.parse(tokenResp.body).token;

  const groupId = getLocationGroupId();
  const locationIds = seedLocations(groupId);

  const userIds = [];
  for (let i = 0; i < seedUsers; i += 1) {
    const username = `seed-user-${i}`;
    const password = "seed-pass";
    const regResp = registerUser(username, password);
    if (![201, 409].includes(regResp.status)) {
      continue;
    }
    const userResp = http.get(`${baseUrl}/api/v1/users/${username}`, {
      tags: { endpoint: "lookup_seed_user" },
    });
    if (userResp.status === 200) {
      userIds.push(JSON.parse(userResp.body).id);
    }
  }

  return { token, userIds, groupId, locationIds };
}

export function loginBurstScenario() {
  const vuId = __VU;
  const iter = __ITER;
  const username = `burst-${vuId}-${iter}`;
  const password = "burst-pass";

  const regResp = registerUser(username, password);
  check(regResp, { "registration status is 201/409": (r) => [201, 409].includes(r.status) });

  const loginResp = loginUser(username, password);
  loginLatencyMs.add(loginResp.timings.duration);
  check(loginResp, { "login status is 200": (r) => r.status === 200 });
  sleep(0.2);
}

export function ratingReadScenario(data) {
  const resp = http.get(`${baseUrl}/api/v1/ratings?sortBy=points&limit=100`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { endpoint: "ratings_read" },
  });
  ratingLatencyMs.add(resp.timings.duration);
  check(resp, { "rating status is 200": (r) => r.status === 200 });
  sleep(0.1);
}

export function heavyWriteScenario(data) {
  if (data.userIds.length === 0 || data.locationIds.length === 0) {
    return;
  }

  const userId = data.userIds[Math.floor(Math.random() * data.userIds.length)];
  const locationId = data.locationIds[Math.floor(Math.random() * data.locationIds.length)];

  const sessionResp = http.post(
    `${baseUrl}/api/v1/gamesessions`,
    JSON.stringify({
      userId,
      locationGroupId: data.groupId,
      totalRounds: 5,
    }),
    {
      headers: { "Content-Type": "application/json" },
      tags: { endpoint: "create_game_session" },
    }
  );
  check(sessionResp, { "session created": (r) => r.status === 201 });
  if (sessionResp.status !== 201) {
    sleep(0.1);
    return;
  }
  const sessionId = JSON.parse(sessionResp.body).id;

  const roundResp = http.post(
    `${baseUrl}/api/v1/gamerounds`,
    JSON.stringify({
      sessionId,
      locationId,
      guessLat: 55.75,
      guessLng: 37.62,
      score: 4000,
      roundNumber: 1,
    }),
    {
      headers: { "Content-Type": "application/json" },
      tags: { endpoint: "create_game_round" },
    }
  );
  writeLatencyMs.add(roundResp.timings.duration);
  check(roundResp, { "round created": (r) => r.status === 201 });
  sleep(0.1);
}
