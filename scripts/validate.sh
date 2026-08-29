#!/usr/bin/env bash
set -euo pipefail

curl --fail --silent --show-error --retry 12 --retry-connrefused --retry-delay 5 http://localhost/actuator/health
