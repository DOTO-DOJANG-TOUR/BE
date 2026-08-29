#!/usr/bin/env bash
set -euo pipefail

systemctl stop doto-compose.service || true
