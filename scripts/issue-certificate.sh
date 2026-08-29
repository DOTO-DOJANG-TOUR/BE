#!/usr/bin/env bash
set -euo pipefail

env_value() {
  sed -n "s/^$1=//p" /opt/app/.env | tail -n 1
}

enable_tls="$(env_value ENABLE_TLS)"
domain_name="$(env_value DOMAIN_NAME)"
letsencrypt_email="$(env_value LETSENCRYPT_EMAIL)"

[ "$enable_tls" = "true" ] || exit 0
test -n "$domain_name"
test -n "$letsencrypt_email"

certificate="/data/letsencrypt/conf/live/$domain_name/fullchain.pem"
[ -f "$certificate" ] && exit 0

docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml run --rm certbot certonly \
  --webroot -w /var/www/certbot \
  --email "$letsencrypt_email" --agree-tos --no-eff-email \
  -d "$domain_name"

cp /opt/app/nginx-https.conf.template /opt/app/nginx.conf
docker compose --env-file /opt/app/.env -f /opt/app/docker-compose.yml up -d --force-recreate nginx
