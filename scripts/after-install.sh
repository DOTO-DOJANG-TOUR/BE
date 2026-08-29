#!/usr/bin/env bash
set -euo pipefail

chown root:root /opt/app/docker-compose.yml /opt/app/nginx-*.conf.template /opt/app/release.env
chmod 644 /opt/app/docker-compose.yml /opt/app/nginx-*.conf.template /opt/app/release.env
test -f /opt/app/.env
chmod 600 /opt/app/.env

env_value() {
  sed -n "s/^$1=//p" /opt/app/.env | tail -n 1
}

enable_tls="$(env_value ENABLE_TLS)"
domain_name="$(env_value DOMAIN_NAME)"
if [ "$enable_tls" = "true" ] && [ -n "$domain_name" ] && [ -f "/data/letsencrypt/conf/live/$domain_name/fullchain.pem" ]; then
  cp /opt/app/nginx-https.conf.template /opt/app/nginx.conf
else
  cp /opt/app/nginx-http.conf.template /opt/app/nginx.conf
fi
