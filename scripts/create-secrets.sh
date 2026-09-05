#!/usr/bin/env bash
# Creates/updates every Secret Manager secret the app needs.
# Run this yourself — it never sends any secret value anywhere except to
# `gcloud secrets`, and prints nothing sensitive to the terminal.
set -euo pipefail

PROJECT=entri-prod
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/secrets.local.env"
TFVARS="$SCRIPT_DIR/../infra/terraform/environments/prod/terraform.tfvars"

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing $ENV_FILE — copy secrets.local.env.example to secrets.local.env and fill it in first." >&2
  exit 1
fi
# shellcheck disable=SC1090
source "$ENV_FILE"

for var in FIREBASE_SERVICE_ACCOUNT_PATH NOVU_SECRET INTASEND_PUBLISHABLE_KEY INTASEND_SECRET_KEY INTASEND_WEBHOOK_CHALLENGE; do
  if [ -z "${!var:-}" ]; then
    echo "secrets.local.env is missing a value for $var" >&2
    exit 1
  fi
done

if [ ! -f "$FIREBASE_SERVICE_ACCOUNT_PATH" ]; then
  echo "FIREBASE_SERVICE_ACCOUNT_PATH ($FIREBASE_SERVICE_ACCOUNT_PATH) does not exist" >&2
  exit 1
fi

secret_exists() {
  gcloud secrets describe "$1" --project="$PROJECT" >/dev/null 2>&1
}

# Only creates the secret if it doesn't exist yet — never overwrites a value
# that's already there (used for things that must stay stable once set:
# passwords, signing keys — changing them after the fact breaks running
# sessions/connections rather than just updating config).
create_once() {
  local name="$1" value="$2"
  if secret_exists "$name"; then
    echo "-> $name already exists, leaving it as-is"
  else
    printf '%s' "$value" | gcloud secrets create "$name" --project="$PROJECT" --data-file=-
    echo "-> created $name"
  fi
}

# Always syncs to the given value — used for plain config (URLs, usernames)
# where re-running the script after a legitimate change should update it.
create_or_update() {
  local name="$1" value="$2"
  if secret_exists "$name"; then
    printf '%s' "$value" | gcloud secrets versions add "$name" --project="$PROJECT" --data-file=-
    echo "-> updated $name"
  else
    printf '%s' "$value" | gcloud secrets create "$name" --project="$PROJECT" --data-file=-
    echo "-> created $name"
  fi
}

create_or_update_from_file() {
  local name="$1" path="$2"
  if secret_exists "$name"; then
    gcloud secrets versions add "$name" --project="$PROJECT" --data-file="$path"
    echo "-> updated $name"
  else
    gcloud secrets create "$name" --project="$PROJECT" --data-file="$path"
    echo "-> created $name"
  fi
}

echo "== Database =="
if [ ! -f "$TFVARS" ]; then
  echo "Can't find $TFVARS" >&2
  exit 1
fi
DB_PASSWORD=$(grep '^db_password' "$TFVARS" | sed -E 's/.*"(.*)".*/\1/')
create_once db-password "$DB_PASSWORD"
create_or_update db-username "entri"

DB_IP=$(gcloud sql instances describe entri-mysql --project="$PROJECT" --format="value(ipAddresses[0].ipAddress)")
create_or_update db-url "jdbc:mysql://${DB_IP}:3306/entri?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true"

echo "== JWT =="
if ! secret_exists jwt-secret; then
  openssl rand -hex 64 | gcloud secrets create jwt-secret --project="$PROJECT" --data-file=-
  echo "-> created jwt-secret"
else
  echo "-> jwt-secret already exists, leaving it as-is"
fi

echo "== RabbitMQ =="
create_or_update rabbitmq-username "entri"
if ! secret_exists rabbitmq-password; then
  openssl rand -base64 24 | gcloud secrets create rabbitmq-password --project="$PROJECT" --data-file=-
  echo "-> created rabbitmq-password"
else
  echo "-> rabbitmq-password already exists, leaving it as-is"
fi

echo "== App config =="
create_or_update app-base-url "https://oro.co.ke"
create_or_update app-api-url "https://oro.co.ke/api"
create_or_update app-cors-allowed-origin "https://oro.co.ke"
create_or_update firebase-storage-bucket "oro-web-app.firebasestorage.app"

echo "== Firebase service account =="
create_or_update_from_file firebase-service-account "$FIREBASE_SERVICE_ACCOUNT_PATH"

echo "== Novu =="
create_or_update novu-secret "$NOVU_SECRET"

echo "== IntaSend =="
create_or_update intasend-publishable-key "$INTASEND_PUBLISHABLE_KEY"
create_or_update intasend-secret-key "$INTASEND_SECRET_KEY"
create_or_update intasend-webhook-challenge "$INTASEND_WEBHOOK_CHALLENGE"

echo
echo "Done. gcloud secrets list --project=$PROJECT to see everything."
