#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: ./scripts/run_local_ci.sh [-u] [-i] [-e] [-r]"
  echo "  -u  run unit tests"
  echo "  -i  run integration tests"
  echo "  -e  run e2e tests"
  echo "  -r  generate Allure report and start local server"
  echo "If no -u/-i/-e flags are provided, all stages run in order: unit -> integration -> e2e"
}

run_unit=false
run_integration=false
run_e2e=false
run_report=false

while getopts ":uierh" opt; do
  case "$opt" in
    u) run_unit=true ;;
    i) run_integration=true ;;
    e) run_e2e=true ;;
    r) run_report=true ;;
    h) usage; exit 0 ;;
    *) usage; exit 1 ;;
  esac
done

if ! $run_unit && ! $run_integration && ! $run_e2e; then
  run_unit=true
  run_integration=true
  run_e2e=true
fi

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
compose_file="$repo_root/../docker-compose.tests.yml"
results_root="$repo_root/allure-results"

mkdir -p "$results_root/unit" "$results_root/integration" "$results_root/e2e"

if [ ! -f "$compose_file" ]; then
  echo "Compose file not found: $compose_file"
  exit 1
fi

if [ "${SHARED_DB:-0}" = "1" ]; then
  export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-shared}"
  echo "Using shared DB with COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT_NAME"
  COMPOSE_RUN_OPTS="--no-deps"
else
  if [ -n "${RUN_ID:-}" ]; then
    export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-${RUN_ID}}"
  else
    export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-run_$(date +%s)_$$}"
  fi
  echo "Using COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT_NAME"
  docker compose -f "$compose_file" up -d db
  COMPOSE_RUN_OPTS=""
  cleanup() {
    docker compose -f "$compose_file" down -v
  }
  trap cleanup EXIT
fi

run_stage() {
  local name="$1"
  local mvn_cmd="$2"
  echo "==> Running $name"
  docker compose -f "$compose_file" run --rm $COMPOSE_RUN_OPTS tests \
    "sed -i 's/\r$//' mvnw scripts/*.sh && chmod +x mvnw scripts/*.sh && ./scripts/wait-for-db.sh db 5432 60 && $mvn_cmd"
}

if $run_unit; then
  run_stage "unit" "./mvnw -B -DskipITs=true -Dallure.results.directory=allure-results/unit -Dallure.skip=true test"
fi

if $run_integration; then
  run_stage "integration" "./mvnw -B -DskipTests=false -DskipITs=false -Dit.test='**/*ITCase' -Dallure.results.directory=allure-results/integration -Dallure.skip=true verify"
fi

if $run_e2e; then
  run_stage "e2e" "./mvnw -B -DskipTests=false -DskipITs=false -Dit.test='**/*E2ECase' -Dallure.results.directory=allure-results/e2e -Dallure.skip=true verify"
fi

if $run_report; then
  echo "==> Generating Allure report"
  cd "$repo_root"
  ./scripts/allure_report.sh

  echo "==> Starting local server at http://localhost:8000"
  cd "$repo_root/target/site/allure-maven-plugin"
  python -m http.server 8000
fi