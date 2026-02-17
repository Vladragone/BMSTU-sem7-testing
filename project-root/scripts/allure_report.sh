#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "$0")/.." && pwd)"
results_root="$root_dir/allure-results"
report_dir="$root_dir/target/site/allure-maven-plugin"
history_cache="$root_dir/target/allure-history"
merged_results_dir="$(mktemp -d -t allure-merged-XXXXXX)"

cleanup() {
  rm -rf "$merged_results_dir" || true
}
trap cleanup EXIT

mkdir -p "$report_dir" || true

if [ -d "$history_cache" ] && [ -w "$history_cache" ] || [ ! -e "$history_cache" ]; then
  mkdir -p "$merged_results_dir/history" || true
  cp -r "$history_cache"/* "$merged_results_dir/history" || true
fi

merge_dir() {
  local src="$1"
  if [ -d "$src" ]; then
    cp -r "$src"/* "$merged_results_dir" || true
  else
    local name="$2"
    local uuid
    uuid="${RANDOM}${RANDOM}"
    cat > "$merged_results_dir/${name}_skipped-result.json" <<EOF
{"uuid":"$uuid","name":"$name","status":"skipped","stage":"finished"}
EOF
  fi
}

merge_dir "$results_root/unit" "unit"
merge_dir "$results_root/integration" "integration"
merge_dir "$results_root/e2e" "e2e"

allure_bin=""
if [ -x "$root_dir/.allure/allure-2.30.0/bin/allure" ]; then
  allure_bin="$root_dir/.allure/allure-2.30.0/bin/allure"
else
  candidate=$(ls -1 "$root_dir/.allure"/allure-*/bin/allure 2>/dev/null | head -n 1 || true)
  if [ -n "$candidate" ] && [ -x "$candidate" ]; then
    allure_bin="$candidate"
  fi
fi

if [ -n "$allure_bin" ]; then
  sed -i 's/\r$//' "$allure_bin" || true
  bash "$allure_bin" generate "$merged_results_dir" -o "$report_dir" --clean
else
  ( cd "$root_dir" && ./mvnw -B -DskipTests -Dallure.results.directory="$merged_results_dir" allure:report )
fi

if [ -d "$report_dir/history" ]; then
  mkdir -p "$history_cache" || true
  cp -r "$report_dir/history"/* "$history_cache" || true
fi
