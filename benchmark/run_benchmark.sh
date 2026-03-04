#!/usr/bin/env bash
set -euo pipefail

RUNS="${RUNS:-100}"
SEED_USERS="${SEED_USERS:-200}"
LOGIN_VUS="${LOGIN_VUS:-80}"
STABLE_RATE="${STABLE_RATE:-60}"
OVERLOAD_RATE="${OVERLOAD_RATE:-140}"
ONLY_HEAVY="${ONLY_HEAVY:-1}"
HEAVY_PROFILE_RPS="${HEAVY_PROFILE_RPS:-100,300,600,900,1200,2000}"
HEAVY_STAGE_DURATION="${HEAVY_STAGE_DURATION:-2m}"
REBUILD_IMAGE_EACH_RUN="${REBUILD_IMAGE_EACH_RUN:-1}"
K6_CPUS="${K6_CPUS:-1.0}"
K6_MEMORY="${K6_MEMORY:-512m}"
BENCH_APP_PORT="${BENCH_APP_PORT:-18080}"
BENCH_PROM_PORT="${BENCH_PROM_PORT:-19090}"
BENCH_DB_PORT="${BENCH_DB_PORT:-15432}"
BENCH_CADVISOR_PORT="${BENCH_CADVISOR_PORT:-18081}"
BENCH_TRACING_ENABLED="${BENCH_TRACING_ENABLED:-false}"
BENCH_TRACING_EXPORTER="${BENCH_TRACING_EXPORTER:-otlp}"
BENCH_TRACING_SERVICE_NAME="${BENCH_TRACING_SERVICE_NAME:-game-bench-app}"
BENCH_TRACING_SAMPLE_RATIO="${BENCH_TRACING_SAMPLE_RATIO:-1.0}"
BENCH_LOG_LEVEL_WEB="${BENCH_LOG_LEVEL_WEB:-ERROR}"
BENCH_LOG_LEVEL_APP="${BENCH_LOG_LEVEL_APP:-ERROR}"
BENCH_LOG_LEVEL_HIBERNATE="${BENCH_LOG_LEVEL_HIBERNATE:-ERROR}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BENCH_DIR="$REPO_ROOT/benchmark"
COMPOSE_FILE="$BENCH_DIR/docker-compose.benchmark.yml"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required"
  exit 1
fi

PYTHON_BIN="python3.12"
if ! command -v "$PYTHON_BIN" >/dev/null 2>&1; then
  PYTHON_BIN="python3"
fi
if ! command -v "$PYTHON_BIN" >/dev/null 2>&1; then
  PYTHON_BIN="python"
fi
if ! command -v "$PYTHON_BIN" >/dev/null 2>&1; then
  echo "python (or python3) is required"
  exit 1
fi

DOCKER_RUN_ENV=()
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*)
    DOCKER_RUN_ENV=(env MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*')
    ;;
esac

STAMP="${CAMPAIGN_STAMP:-$(date +%Y%m%d-%H%M%S)}"
CAMPAIGN_DIR="$BENCH_DIR/results/$STAMP"
mkdir -p "$CAMPAIGN_DIR"

cat >"$CAMPAIGN_DIR/campaign_config.json" <<EOF
{
  "runs": $RUNS,
  "seed_users": $SEED_USERS,
  "login_vus": $LOGIN_VUS,
  "stable_rate": $STABLE_RATE,
  "overload_rate": $OVERLOAD_RATE,
  "only_heavy": $ONLY_HEAVY,
  "heavy_profile_rps": "$HEAVY_PROFILE_RPS",
  "heavy_stage_duration": "$HEAVY_STAGE_DURATION",
  "k6_limits": {
    "cpus": "$K6_CPUS",
    "memory": "$K6_MEMORY"
  },
  "component_limits": {
    "app_port": "$BENCH_APP_PORT",
    "prometheus_port": "$BENCH_PROM_PORT",
    "db_port": "$BENCH_DB_PORT",
    "cadvisor_port": "$BENCH_CADVISOR_PORT"
  },
  "observability": {
    "tracing_enabled": "$BENCH_TRACING_ENABLED",
    "tracing_exporter": "$BENCH_TRACING_EXPORTER",
    "tracing_service_name": "$BENCH_TRACING_SERVICE_NAME",
    "tracing_sample_ratio": "$BENCH_TRACING_SAMPLE_RATIO",
    "logging_levels": {
      "org.springframework.web": "$BENCH_LOG_LEVEL_WEB",
      "com.example.game": "$BENCH_LOG_LEVEL_APP",
      "org.hibernate": "$BENCH_LOG_LEVEL_HIBERNATE"
    }
  }
}
EOF

echo "Campaign dir: $CAMPAIGN_DIR"
echo "Runs to execute: $RUNS"

for i in $(seq 1 "$RUNS"); do
  run_id="$(printf "run-%03d" "$i")"
  run_dir="$CAMPAIGN_DIR/$run_id"
  mkdir -p "$run_dir"

  project="bmstu_lab3_${STAMP}_${i}"
  export COMPOSE_PROJECT_NAME="$project"
  export BENCH_APP_PORT BENCH_PROM_PORT BENCH_DB_PORT BENCH_CADVISOR_PORT
  export BENCH_TRACING_ENABLED BENCH_TRACING_EXPORTER BENCH_TRACING_SERVICE_NAME BENCH_TRACING_SAMPLE_RATIO
  export BENCH_LOG_LEVEL_WEB BENCH_LOG_LEVEL_APP BENCH_LOG_LEVEL_HIBERNATE

  echo ""
  echo "=== $run_id/$RUNS ==="

  cleanup() {
    docker compose -f "$COMPOSE_FILE" down -v --remove-orphans >/dev/null 2>&1 || true
  }
  trap cleanup EXIT
  cleanup

  if [ "$REBUILD_IMAGE_EACH_RUN" = "1" ]; then
    docker compose -f "$COMPOSE_FILE" build --no-cache app
  else
    docker compose -f "$COMPOSE_FILE" build app
  fi

  docker compose -f "$COMPOSE_FILE" up -d db app cadvisor prometheus otel-collector

  wait_in_network() {
    local url="$1"
    local timeout="$2"
    local deadline=$((SECONDS + timeout))
    while [ "$SECONDS" -lt "$deadline" ]; do
      if "${DOCKER_RUN_ENV[@]}" docker run --rm --network "${project}_default" curlimages/curl:8.11.1 \
        -fsS "$url" >/dev/null 2>&1; then
        return 0
      fi
      sleep 2
    done
    echo "Timeout waiting for $url (inside docker network)"
    return 1
  }

  wait_in_network "http://app:8080/api/v1/location-groups" 180
  wait_in_network "http://prometheus:9090/-/ready" 120

  run_start="$(date +%s)"
  "${DOCKER_RUN_ENV[@]}" docker run --rm \
    --cpus="$K6_CPUS" \
    --memory="$K6_MEMORY" \
    --network "${project}_default" \
    -e BASE_URL="http://app:8080" \
    -e SEED_USERS="$SEED_USERS" \
    -e LOGIN_VUS="$LOGIN_VUS" \
    -e STABLE_RATE="$STABLE_RATE" \
    -e OVERLOAD_RATE="$OVERLOAD_RATE" \
    -e ONLY_HEAVY="$ONLY_HEAVY" \
    -e HEAVY_PROFILE_RPS="$HEAVY_PROFILE_RPS" \
    -e HEAVY_STAGE_DURATION="$HEAVY_STAGE_DURATION" \
    -v "$REPO_ROOT:/workspace" \
    grafana/k6:0.53.0 run \
    /workspace/benchmark/k6/lab3_scenarios.js \
    --summary-export="/workspace/benchmark/results/$STAMP/$run_id/k6-summary.json" \
    --out "json=/workspace/benchmark/results/$STAMP/$run_id/k6-metrics.json"
  run_end="$(date +%s)"

  cat >"$run_dir/run_timing.json" <<EOF
{
  "run_start_unix": $run_start,
  "run_end_unix": $run_end
}
EOF

  if [ ! -f "$run_dir/k6-metrics.json" ] || [ ! -f "$run_dir/k6-summary.json" ]; then
    echo "Missing k6 artifacts in $run_dir (k6-metrics.json and/or k6-summary.json)"
    exit 1
  fi

  "$PYTHON_BIN" "$BENCH_DIR/scripts/collect_prometheus_stats.py" \
    --prom-url "http://localhost:${BENCH_PROM_PORT}" \
    --start "$run_start" \
    --end "$run_end" \
    --services app db \
    --out "$run_dir/resources-summary.json"

  if [ ! -f "$run_dir/resources-summary.json" ] || \
     [ ! -f "$run_dir/resources_app_over_time.png" ] || \
     [ ! -f "$run_dir/resources_db_over_time.png" ] || \
     [ ! -f "$run_dir/resources_timeseries_app.csv" ] || \
     [ ! -f "$run_dir/resources_timeseries_db.csv" ]; then
    echo "Missing Prometheus resource artifacts in $run_dir"
    exit 1
  fi

  "$PYTHON_BIN" "$BENCH_DIR/scripts/analyze_k6_run.py" \
    --k6-json "$run_dir/k6-metrics.json" \
    --out-dir "$run_dir" \
    --scenario heavy_overload_recovery \
    --stage-duration "$HEAVY_STAGE_DURATION" \
    --profile-rps "$HEAVY_PROFILE_RPS"

  if [ ! -f "$run_dir/latency_over_time.png" ] || \
     [ ! -f "$run_dir/latency_percentiles.png" ] || \
     [ ! -f "$run_dir/latency_histogram.png" ]; then
    echo "Missing latency artifacts in $run_dir"
    exit 1
  fi

  collector_cid="$(docker compose -f "$COMPOSE_FILE" ps -q otel-collector)"
  if [ -n "$collector_cid" ]; then
    docker cp "$collector_cid:/var/lib/otel/traces.jsonl" "$run_dir/traces-otel.jsonl" >/dev/null 2>&1 || true
  fi

  cleanup
  trap - EXIT
done

"$PYTHON_BIN" "$BENCH_DIR/scripts/aggregate_campaign.py" --campaign-dir "$CAMPAIGN_DIR"
echo "Campaign completed: $CAMPAIGN_DIR"
