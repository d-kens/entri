#!/usr/bin/env bash
# Run this on the VM: ./deploy-rabbitmq.sh
# Applies whatever rabbitmq/definitions.json currently contains, live, via
# the management HTTP API — no broker restart needed. Triggered by
# deploy-rabbitmq.yml only when rabbitmq/** (or docker-compose.yml) changes.
#
# Definitions are NOT loaded at boot. RabbitMQ's `load_definitions` runs
# before the default vhost "/" exists on a node that's booting fresh, and
# fails hard with "Please create virtual host / prior to importing
# definitions" (crash-looped prod on 2026-09-06). Importing live via the API
# runs after the broker already reports healthy, when the vhost always
# exists, so it can't hit that race.
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_DIR"

echo "==> Validating rabbitmq/definitions.json"
if ! python3 -c "import json; json.load(open('rabbitmq/definitions.json'))"; then
  echo "==> definitions.json is not valid JSON — not applying" >&2
  exit 1
fi

echo "==> Ensuring rabbitmq is up"
docker compose up -d --no-deps --wait --wait-timeout 60 rabbitmq

source .env
RMQ_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$(docker compose ps -q rabbitmq)")

echo "==> Importing definitions"
curl -sf -u "$RABBITMQ_USERNAME:$RABBITMQ_PASSWORD" \
  -H "Content-Type: application/json" \
  -X POST "http://$RMQ_IP:15672/api/definitions" \
  --data-binary @rabbitmq/definitions.json

echo "==> rabbitmq definitions applied"
