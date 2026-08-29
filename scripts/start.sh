#!/usr/bin/env bash
set -euo pipefail

app_image="$(sed -n 's/^APP_IMAGE=//p' /opt/app/release.env | tail -n 1)"
test -n "$app_image"
registry="${app_image%%/*}"
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin "$registry"

sed -i '/^APP_IMAGE=/d' /opt/app/.env
printf 'APP_IMAGE=%s\n' "$app_image" >> /opt/app/.env

docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml pull
systemctl start doto-compose.service
script_dir="$(cd "$(dirname "$0")" && pwd)"
"$script_dir/issue-certificate.sh"
