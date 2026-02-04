#!/usr/bin/env bash
set -euo pipefail

output="${1:-traffic.pcap}"
base_url="${BASE_URL:-http://localhost:8080}"

sudo tcpdump -i any -w "$output" "tcp port 8080" &
TCPDUMP_PID=$!

trap "sudo kill $TCPDUMP_PID" EXIT

BASE_URL="$base_url" ./scripts/e2e_requests.sh

sudo kill $TCPDUMP_PID
trap - EXIT

echo "Traffic capture saved to $output"