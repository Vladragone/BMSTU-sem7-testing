#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: ./scripts/run_local_ci.sh [-u] [-i] [-e]"
  echo "  -u  run unit tests"
  echo "  -i  run integration tests"
  echo "  -e  run e2e tests"
  echo "If no flags are provided, all stages run in order: unit -> integration -> e2e"
}

run_unit=false
run_integration=false
run_e2e=false

while getopts ":uieh" opt; do
  case "$opt" in
    u) run_unit=true ;;
    i) run_integration=true ;;
    e) run_e2e=true ;;
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

if [ ! -f "$compose_file" ]; then
  echo "Compose file not found: $compose_file"
  exit 1
fi

docker compose -f "$compose_file" up -d db

cleanup() {
  docker compose -f "$compose_file" down -v
}
trap cleanup EXIT

run_stage() {
  local name="$1"
  local mvn_cmd="$2"
  echo "==> Running $name"
  docker compose -f "$compose_file" run --rm tests \
    "sed -i 's/\r$//' mvnw scripts/*.sh && chmod +x mvnw scripts/*.sh && ./scripts/wait-for-db.sh db 5432 60 && $mvn_cmd"
}

if $run_unit; then
  run_stage "unit" "./mvnw -B -DskipITs=true -Dallure.results.directory=target/allure-results/unit test"
fi

if $run_integration; then
  run_stage "integration" "./mvnw -B -DskipTests=true -DskipITs=false -Dit.test='**/*ITCase' -Dallure.results.directory=target/allure-results/integration verify"
fi

if $run_e2e; then
  run_stage "e2e" "./mvnw -B -DskipTests=true -DskipITs=false -Dit.test='**/*E2ECase' -Dallure.results.directory=target/allure-results/e2e verify"
fi