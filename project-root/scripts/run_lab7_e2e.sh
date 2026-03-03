#!/usr/bin/env bash
set -euo pipefail

mode="${1:-mock}"
repo_root="$(cd "$(dirname "$0")/.." && pwd)"
compose_file="$repo_root/../docker-compose.tests.yml"

if [[ "$mode" != "mock" && "$mode" != "real" ]]; then
  echo "Usage: ./scripts/run_lab7_e2e.sh [mock|real]"
  exit 1
fi

if [[ "$mode" == "mock" ]]; then
  docker compose -f "$compose_file" up -d db weather-mock
  docker compose -f "$compose_file" run --rm -e EXTERNAL_WEATHER_MODE=mock tests \
    "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipTests=false -DskipITs=false -Dit.test='**/*WeatherMockE2ECase' -Dallure.skip=true verify"
else
  docker compose -f "$compose_file" up -d db
  docker compose -f "$compose_file" run --rm -e RUN_REAL_WEATHER_E2E=true -e EXTERNAL_WEATHER_MODE=real tests \
    "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipTests=false -DskipITs=false -Dit.test='**/*WeatherRealE2ECase' -Dallure.skip=true verify"
fi
