#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RUNS="${RUNS:-1}"
GC_OLD_LABEL="${GC_OLD_LABEL:-old_gc}"
GC_NEW_LABEL="${GC_NEW_LABEL:-new_gc}"
GC_OLD_OPTS="${GC_OLD_OPTS:--XX:+UseG1GC}"
GC_NEW_OPTS="${GC_NEW_OPTS:--XX:+UseZGC}"
BASE_STAMP="${BASE_STAMP:-$(date +%Y%m%d-%H%M%S)_gc_compare}"
PYTHON_BIN="${PYTHON_BIN:-python3.12}"

OLD_STAMP="${BASE_STAMP}_${GC_OLD_LABEL}"
NEW_STAMP="${BASE_STAMP}_${GC_NEW_LABEL}"

echo "Running $GC_OLD_LABEL campaign with: $GC_OLD_OPTS"
CAMPAIGN_STAMP="$OLD_STAMP" \
BENCH_JAVA_TOOL_OPTIONS="$GC_OLD_OPTS" \
RUNS="$RUNS" \
bash "$REPO_ROOT/benchmark/run_benchmark.sh"

echo "Running $GC_NEW_LABEL campaign with: $GC_NEW_OPTS"
CAMPAIGN_STAMP="$NEW_STAMP" \
BENCH_JAVA_TOOL_OPTIONS="$GC_NEW_OPTS" \
RUNS="$RUNS" \
bash "$REPO_ROOT/benchmark/run_benchmark.sh"

COMPARE_OUT_DIR="$REPO_ROOT/benchmark/results/${BASE_STAMP}_compare"
mkdir -p "$COMPARE_OUT_DIR"

for i in $(seq 1 "$RUNS"); do
  run_id="$(printf "run-%03d" "$i")"
  old_run_dir="$REPO_ROOT/benchmark/results/$OLD_STAMP/$run_id"
  new_run_dir="$REPO_ROOT/benchmark/results/$NEW_STAMP/$run_id"
  out_dir="$COMPARE_OUT_DIR/$run_id"
  mkdir -p "$out_dir"

  "$PYTHON_BIN" "$REPO_ROOT/benchmark/scripts/compare_gc_runs.py" \
    --old-run-dir "$old_run_dir" \
    --new-run-dir "$new_run_dir" \
    --out-dir "$out_dir" \
    --label-old "$GC_OLD_LABEL" \
    --label-new "$GC_NEW_LABEL"
done

cat >"$COMPARE_OUT_DIR/compare_config.json" <<EOF
{
  "runs": $RUNS,
  "old": {
    "label": "$GC_OLD_LABEL",
    "java_tool_options": "$GC_OLD_OPTS",
    "campaign_stamp": "$OLD_STAMP"
  },
  "new": {
    "label": "$GC_NEW_LABEL",
    "java_tool_options": "$GC_NEW_OPTS",
    "campaign_stamp": "$NEW_STAMP"
  }
}
EOF

echo "GC comparison completed: $COMPARE_OUT_DIR"
