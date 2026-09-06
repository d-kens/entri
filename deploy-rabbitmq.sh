#!/usr/bin/env bash
# Run this on the VM: ./deploy-rabbitmq.sh
# Applies whatever rabbitmq/definitions.json currently contains, live, via
# `rabbitmqctl import_definitions` — no broker restart needed. Triggered by
# deploy-rabbitmq.yml only when rabbitmq/** (or docker-compose.yml) changes.
#
# Definitions are NOT loaded at boot. RabbitMQ's `management.load_definitions`
# runs before the default vhost "/" exists on a node booting fresh, and fails
# hard with "Please create virtual host / prior to importing definitions"
# (crash-looped prod on 2026-09-06). `rabbitmqctl import_definitions` runs
# against the already-running node over the local Erlang connection inside
# the container, so — unlike a curl against the management API — it needs no
# network path to the container's internal IP and no credentials.
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

echo "==> Importing definitions"
docker compose exec -T rabbitmq rabbitmqctl import_definitions /etc/rabbitmq/definitions.json

echo "==> rabbitmq definitions applied"
