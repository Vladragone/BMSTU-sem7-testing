#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "$0")/.." && pwd)"
results_root="$root_dir/allure-results"
report_dir="$root_dir/target/site/allure-maven-plugin"
history_cache="$root_dir/target/allure-history"

mkdir -p "$results_root"

if [ -d "$history_cache" ]; then
  mkdir -p "$results_root/history"
  cp -r "$history_cache"/* "$results_root/history" || true
fi

merge_dir() {
  local src="$1"
  if [ -d "$src" ]; then
    cp -r "$src"/* "$results_root" || true
  else
    local name="$2"
    local uuid
    uuid="${RANDOM}${RANDOM}"
    cat > "$results_root/${name}_skipped-result.json" <<EOF
{"uuid":"$uuid","name":"$name","status":"skipped","stage":"finished"}
EOF
  fi
}

merge_dir "$results_root/unit" "unit"
merge_dir "$results_root/integration" "integration"
merge_dir "$results_root/e2e" "e2e"

mkdir -p "$report_dir"

# Prefer bundled Allure CLI to avoid Maven plugin resolution issues
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
  "$allure_bin" generate "$results_root" -o "$report_dir" --clean
else
  ( cd "$root_dir" && ./mvnw -B -DskipTests -Dallure.results.directory=allure-results allure:report )
fi

if [ -d "$report_dir/history" ]; then
  mkdir -p "$history_cache"
  cp -r "$report_dir/history"/* "$history_cache" || true
fi