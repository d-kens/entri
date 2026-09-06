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

# `up -d` only recreates a container when its service definition changes
# (image, env, volume list) — not when a bind-mounted file's *contents*
# change, which is exactly what a prometheus.yml/dashboard JSON edit is. Both
# already-running containers would otherwise keep serving their stale
# in-memory config from before this deploy. Prometheus reloads its config
# on SIGHUP with no extra flags needed; Grafana's file-based provisioning
# has no such signal, so it's a full restart instead.
echo "==> Reloading prometheus config"
docker compose exec -T prometheus kill -HUP 1
echo "==> Restarting grafana to pick up provisioning changes"
docker compose restart grafana

echo "==> monitoring stack applied"
