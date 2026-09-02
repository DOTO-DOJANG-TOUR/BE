#!/usr/bin/env bash
set -euo pipefail

compose=(docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml)

for _ in $(seq 1 12); do
  app_container="$("${compose[@]}" ps -q app)"
  if [ -n "$app_container" ] && [ "$(docker inspect --format '{{.State.Health.Status}}' "$app_container")" = "healthy" ]; then
    exit 0
  fi
  sleep 5
done

"${compose[@]}" ps
"${compose[@]}" logs --tail=100 app
exit 1
