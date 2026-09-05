#!/usr/bin/env bash
# Run this on the VM: ./deploy.sh <api|web> <tag>
# Pulls the given image tag from Artifact Registry and restarts only that
# one service. Also refreshes .env from Secret Manager every run, so a
# secret rotation takes effect on the next deploy of either service.
set -euo pipefail

SERVICE="${1:-}"
TAG="${2:-}"

if [[ "$SERVICE" != "api" && "$SERVICE" != "web" ]] || [ -z "$TAG" ]; then
  echo "Usage: ./deploy.sh <api|web> <tag>" >&2
  exit 1
fi

PROJECT=entri-prod
REGISTRY="africa-south1-docker.pkg.dev/${PROJECT}/entri"
REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_DIR"

secret() {
  gcloud secrets versions access latest --secret="$1" --project="$PROJECT"
}

echo "==> Authenticating docker to Artifact Registry"
gcloud auth configure-docker africa-south1-docker.pkg.dev --quiet

echo "==> Writing Firebase service account"
sudo mkdir -p /etc/entri
secret firebase-service-account | sudo tee /etc/entri/firebase.json >/dev/null
sudo chmod 644 /etc/entri/firebase.json

# Keep whichever service isn't being deployed on its currently running image —
# only the target service's tag actually changes.
current_image() {
  local cid
  cid=$(docker compose ps -q "$1" 2>/dev/null || true)
  if [ -n "$cid" ]; then
    docker inspect --format '{{.Config.Image}}' "$cid"
  else
    echo "${REGISTRY}/$1:latest"
  fi
}
API_IMAGE=$(current_image api)
WEB_IMAGE=$(current_image web)
if [ "$SERVICE" = "api" ]; then API_IMAGE="${REGISTRY}/api:${TAG}"; fi
if [ "$SERVICE" = "web" ]; then WEB_IMAGE="${REGISTRY}/web:${TAG}"; fi

echo "==> Pulling secrets into .env"
cat > .env <<EOF
API_IMAGE=${API_IMAGE}
WEB_IMAGE=${WEB_IMAGE}
SPRING_DATASOURCE_URL=$(secret db-url)
SPRING_DATASOURCE_USERNAME=$(secret db-username)
SPRING_DATASOURCE_PASSWORD=$(secret db-password)
RABBITMQ_USERNAME=$(secret rabbitmq-username)
RABBITMQ_PASSWORD=$(secret rabbitmq-password)
JWT_SECRET=$(secret jwt-secret)
APP_BASE_URL=$(secret app-base-url)
APP_API_URL=$(secret app-api-url)
APP_CORS_ALLOWED_ORIGIN=$(secret app-cors-allowed-origin)
FIREBASE_STORAGE_BUCKET=$(secret firebase-storage-bucket)
NOVU_SECRET=$(secret novu-secret)
INTASEND_PUBLISHABLE_KEY=$(secret intasend-publishable-key)
INTASEND_SECRET_KEY=$(secret intasend-secret-key)
INTASEND_WEBHOOK_CHALLENGE=$(secret intasend-webhook-challenge)
EOF
chmod 600 .env


# nginx/ (including its snippets/) is synced on every deploy regardless of which
# service was targeted (see the CI workflow), but that's just files landing on
# disk — nothing reloads nginx to pick them up, and touching nginx on every
# routine api/web deploy (most of which don't change it at all) would add an
# unrelated failure mode to deploys that have nothing to do with nginx. Only
# validate/apply when the synced files actually changed since the last deploy.
NGINX_HASH_FILE=".nginx-hash"
NGINX_HASH=$(find nginx -type f -exec sha256sum {} \; | sort | sha256sum | awk '{print $1}')

if [ ! -f "$NGINX_HASH_FILE" ] || [ "$(cat "$NGINX_HASH_FILE")" != "$NGINX_HASH" ]; then
  echo "==> nginx config changed — validating"
  # `compose run` builds a fresh, throwaway container from the *current*
  # docker-compose.yml (picking up any new bind mounts, e.g. snippets/) rather
  # than exec-ing into the already-running nginx container, which may still be
  # on the old container definition and missing a mount the new config needs.
  if ! docker compose run --rm --no-deps --entrypoint nginx nginx -t; then
    echo "==> nginx config test failed — not applying" >&2
    exit 1
  fi

  echo "==> Applying nginx config"
  docker compose up -d --no-deps nginx
  docker compose exec -T nginx nginx -s reload
  echo "$NGINX_HASH" > "$NGINX_HASH_FILE"
else
  echo "==> nginx config unchanged — skipping reload"
fi

echo "==> Pulling ${SERVICE}:${TAG}"
docker compose pull "$SERVICE"

echo "==> Restarting ${SERVICE}"
docker compose up -d "$SERVICE"

echo "==> Waiting for ${SERVICE} to become healthy"
for _ in $(seq 1 30); do
  cid=$(docker compose ps -q "$SERVICE")
  status=$(docker inspect --format '{{.State.Health.Status}}' "$cid" 2>/dev/null || echo "unknown")
  if [ "$status" = "healthy" ]; then
    echo "==> ${SERVICE} is healthy"
    exit 0
  fi
  sleep 2
done

echo "==> ${SERVICE} did not become healthy in time" >&2
docker compose logs --tail=50 "$SERVICE" >&2
exit 1
