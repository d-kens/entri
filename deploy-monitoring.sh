#!/usr/bin/env bash
# Run this on the VM: ./deploy-monitoring.sh
# Validates and applies whatever monitoring/ currently contains. Triggered by
# deploy-monitoring.yml only when monitoring/** changes on master, so — unlike
# deploy.sh — there's no need to detect "did this actually change": the
# workflow trigger already answered that.
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_DIR"

echo "==> Validating prometheus config"
if ! docker compose run --rm --no-deps --entrypoint promtool prometheus check config /etc/prometheus/prometheus.yml; then
  echo "==> prometheus config check failed — not applying" >&2
  exit 1
fi

echo "==> Applying monitoring stack"
docker compose up -d --no-deps --wait --wait-timeout 60 prometheus grafana

echo "==> monitoring stack applied"
