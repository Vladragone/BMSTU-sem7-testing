#!/usr/bin/env bash
set -euo pipefail

host="${1:-db}"
port="${2:-5432}"
timeout="${3:-60}"

start="$(date +%s)"
while true; do
  if nc -z "$host" "$port"; then
    echo "Database is ready on $host:$port"
    exit 0
  fi
  if [ $(( $(date +%s) - start )) -ge "$timeout" ]; then
    echo "Timeout waiting for database at $host:$port"
    exit 1
  fi
  sleep 2
done