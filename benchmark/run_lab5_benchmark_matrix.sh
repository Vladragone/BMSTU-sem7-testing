#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BENCH_DIR="$REPO_ROOT/benchmark"

RUNS_PER_PROFILE="${RUNS_PER_PROFILE:-3}"
MATRIX_STAMP="${MATRIX_STAMP:-lab5-$(date +%Y%m%d-%H%M%S)}"
MATRIX_DIR="$BENCH_DIR/results/$MATRIX_STAMP"
mkdir -p "$MATRIX_DIR"

profiles=("baseline" "tracing" "logging_extended" "tracing_logging_extended")

for profile in "${profiles[@]}"; do
  tracing_enabled="false"
  log_web="ERROR"
  log_app="ERROR"
  log_hibernate="ERROR"

  if [ "$profile" = "tracing" ]; then
    tracing_enabled="true"
  elif [ "$profile" = "logging_extended" ]; then
    log_web="INFO"
    log_app="DEBUG"
    log_hibernate="WARN"
  elif [ "$profile" = "tracing_logging_extended" ]; then
    tracing_enabled="true"
    log_web="INFO"
    log_app="DEBUG"
    log_hibernate="WARN"
  fi

  profile_stamp="${MATRIX_STAMP}-${profile}"
  echo "Running profile=$profile (runs=$RUNS_PER_PROFILE, stamp=$profile_stamp)"

  RUNS="$RUNS_PER_PROFILE" \
  CAMPAIGN_STAMP="$profile_stamp" \
  BENCH_TRACING_ENABLED="$tracing_enabled" \
  BENCH_TRACING_EXPORTER="otlp" \
  BENCH_LOG_LEVEL_WEB="$log_web" \
  BENCH_LOG_LEVEL_APP="$log_app" \
  BENCH_LOG_LEVEL_HIBERNATE="$log_hibernate" \
  "$BENCH_DIR/run_benchmark.sh"
done

python3 "$BENCH_DIR/scripts/aggregate_lab5_matrix.py" \
  --results-root "$BENCH_DIR/results" \
  --matrix-stamp "$MATRIX_STAMP" \
  --out-dir "$MATRIX_DIR"

echo "Lab5 benchmark matrix completed: $MATRIX_DIR"
