#!/usr/bin/env bash
# Run this on the VM: ./deploy-rabbitmq.sh
# Applies whatever rabbitmq/ currently contains. Triggered by
# deploy-rabbitmq.yml only when rabbitmq/** (or docker-compose.yml) changes
# on master, so — unlike deploy.sh — there's no need to detect "did this
# actually change": the workflow trigger already answered that.
#
# RabbitMQ only reads `load_definitions` at node boot — there's no live
# reload — so applying a policy change means recreating the container. This
# briefly drops broker connections; Spring AMQP's CachingConnectionFactory
# reconnects automatically, the same as it does on any other rabbitmq
# restart, so this is safe to run against prod.
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_DIR"

echo "==> Validating rabbitmq/definitions.json"
if ! python3 -c "import json; json.load(open('rabbitmq/definitions.json'))"; then
  echo "==> definitions.json is not valid JSON — not applying" >&2
  exit 1
fi

echo "==> Applying RabbitMQ config"
docker compose up -d --no-deps --force-recreate --wait --wait-timeout 60 rabbitmq

echo "==> rabbitmq applied"
