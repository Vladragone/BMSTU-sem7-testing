#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$project_root"

echo "Running quality gate: Checkstyle + PMD(Cyclomatic<=10) + Halstead + compilation..."
./mvnw -B -DskipTests=true -DskipITs=true process-classes
