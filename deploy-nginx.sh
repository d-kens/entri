#!/usr/bin/env bash
# Run this on the VM: ./deploy-nginx.sh
# Validates and applies whatever nginx/ currently contains. Triggered by
# deploy-nginx.yml only when nginx/** changes on master, so — unlike
# deploy.sh — there's no need to detect "did this actually change": the
# workflow trigger already answered that.
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_DIR"

echo "==> Validating nginx config"
# `compose run` builds a fresh, throwaway container from the *current*
# docker-compose.yml (picking up any new bind mounts, e.g. snippets/) rather
# than exec-ing into the already-running nginx container, which may still be
# on the old container definition and missing a mount the new config needs.
if ! docker compose run --rm --no-deps --entrypoint nginx nginx -t; then
  echo "==> nginx config test failed — not applying" >&2
  exit 1
fi

echo "==> Applying nginx config"
docker compose up -d --no-deps --wait --wait-timeout 30 nginx
docker compose exec -T nginx nginx -s reload

echo "==> nginx config applied"
