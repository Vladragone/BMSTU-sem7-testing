#!/usr/bin/env bash
set -euo pipefail

base_url="${BASE_URL:-http://localhost:8080}"
user1="${USER1:-e2e_cli_1}"
user2="${USER2:-e2e_cli_2}"
pass1="${PASS1:-pass1}"
pass2="${PASS2:-pass2}"
email1="${EMAIL1:-e2e_cli_1@example.com}"
email2="${EMAIL2:-e2e_cli_2@example.com}"

json_post() {
  local url="$1"
  local data="$2"
  curl -sS -H "Content-Type: application/json" -d "$data" "$url"
}

register_user() {
  local user="$1"
  local email="$2"
  local pass="$3"
  json_post "$base_url/api/v1/users" "{\"username\":\"$user\",\"email\":\"$email\",\"password\":\"$pass\"}" > /dev/null
}

login() {
  local user="$1"
  local pass="$2"
  json_post "$base_url/api/v1/tokens" "{\"username\":\"$user\",\"password\":\"$pass\"}" \
    | python -c "import sys,json; print(json.load(sys.stdin)['token'])"
}

update_profile() {
  local token="$1"
  local score="$2"
  curl -sS -X PATCH "$base_url/api/v1/profiles/me" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token" \
    -d "{\"score\":$score}" > /dev/null
}

register_user "$user1" "$email1" "$pass1"
register_user "$user2" "$email2" "$pass2"

token1="$(login "$user1" "$pass1")"
token2="$(login "$user2" "$pass2")"

update_profile "$token1" 100
update_profile "$token2" 50

curl -sS -H "Authorization: Bearer $token1" "$base_url/api/v1/profiles/me" > /dev/null
curl -sS -H "Authorization: Bearer $token1" "$base_url/api/v1/ratings?sortBy=points&limit=10" > /dev/null

echo "E2E requests completed"