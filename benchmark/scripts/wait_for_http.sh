#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <url> <timeout_seconds>"
  exit 1
fi

URL="$1"
TIMEOUT="$2"
DEADLINE=$((SECONDS + TIMEOUT))

while [ "$SECONDS" -lt "$DEADLINE" ]; do
  if curl -fsS "$URL" >/dev/null 2>&1; then
    exit 0
  fi
  sleep 2
done

echo "Timeout waiting for $URL"
exit 1
