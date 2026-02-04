#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "$0")/.." && pwd)"
results_dir="$root_dir/target/allure-results"
report_dir="$root_dir/target/site/allure-maven-plugin"
history_cache="$root_dir/target/allure-history"

mkdir -p "$results_dir"

if [ -d "$history_cache" ]; then
  mkdir -p "$results_dir/history"
  cp -r "$history_cache"/* "$results_dir/history" || true
fi

merge_dir() {
  local src="$1"
  if [ -d "$src" ]; then
    cp -r "$src"/* "$results_dir" || true
  else
    local name="$2"
    local uuid
    uuid="${RANDOM}${RANDOM}"
    cat > "$results_dir/${name}_skipped-result.json" <<EOF
{"uuid":"$uuid","name":"$name","status":"skipped","stage":"finished"}
EOF
  fi
}

merge_dir "$results_dir/unit" "unit"
merge_dir "$results_dir/integration" "integration"
merge_dir "$results_dir/e2e" "e2e"

( cd "$root_dir" && ./mvnw -B -DskipTests -Dallure.results.directory=target/allure-results allure:report )

if [ -d "$report_dir/history" ]; then
  mkdir -p "$history_cache"
  cp -r "$report_dir/history"/* "$history_cache" || true
fi