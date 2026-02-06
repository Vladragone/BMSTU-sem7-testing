#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "$0")/.." && pwd)"
output_dir="$root_dir/traffic"
mkdir -p "$output_dir"

output="$output_dir/${1:-traffic.pcap}"
base_url="${BASE_URL:-http://localhost:8080}"

sudo tcpdump -i any -w "$output" "tcp port 8080" &
TCPDUMP_PID=$!

trap "sudo kill $TCPDUMP_PID" EXIT

BASE_URL="$base_url" ./scripts/e2e_requests.sh

sudo kill $TCPDUMP_PID
trap - EXIT

echo "Traffic capture saved to $output"
