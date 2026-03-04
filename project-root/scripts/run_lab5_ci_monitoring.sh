#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_BASE="$PROJECT_ROOT/lab5-monitoring/ci"
STAMP="${LAB5_MONITORING_STAMP:-$(date +%Y%m%d-%H%M%S)}"
OUT_DIR="$OUT_BASE/$STAMP"
mkdir -p "$OUT_DIR"

if [ ! -x ./mvnw ]; then
  echo "run from project-root directory"
  exit 1
fi

run_case() {
  local profile="$1"
  local tracing_enabled="$2"
  local log_web="$3"
  local log_app="$4"
  local log_hibernate="$5"

  local timing_file="$OUT_DIR/${profile}-timing.txt"
  local log_file="$OUT_DIR/${profile}.log"

  TRACING_ENABLED="$tracing_enabled" \
  TRACING_EXPORTER="logging" \
  LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB="$log_web" \
  LOGGING_LEVEL_COM_EXAMPLE_GAME="$log_app" \
  LOGGING_LEVEL_ORG_HIBERNATE="$log_hibernate" \
  /usr/bin/time -f "user_seconds=%U\nsystem_seconds=%S\ncpu_percent=%P\nmax_rss_kb=%M\nelapsed_seconds=%e" \
    -o "$timing_file" \
    ./mvnw -B -DskipTests=true -DskipITs=false -Dit.test='**/*AuthServiceITCase' verify \
    >"$log_file" 2>&1
}

echo "Running lab5 CI monitoring matrix into $OUT_DIR"
run_case baseline false ERROR ERROR ERROR
run_case tracing true ERROR ERROR ERROR
run_case logging_extended false INFO DEBUG WARN
run_case tracing_logging_extended true INFO DEBUG WARN

python3 "$PROJECT_ROOT/../benchmark/scripts/aggregate_lab5_ci_monitoring.py" \
  --input-dir "$OUT_DIR" \
  --out-dir "$OUT_DIR"

echo "CI monitoring artifacts: $OUT_DIR"
