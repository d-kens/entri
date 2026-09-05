#!/usr/bin/env bash
set -euo pipefail

# Idempotent: this runs on every boot, not just the first one.

if ! command -v docker >/dev/null; then
  apt-get update
  apt-get install -y ca-certificates curl certbot gnupg

  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu jammy stable" \
    > /etc/apt/sources.list.d/docker.list

  apt-get update
  apt-get install -y docker-ce docker-ce-cli docker-compose-plugin
fi

if ! command -v gcloud >/dev/null; then
  install -m 0755 -d /usr/share/keyrings
  curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
  echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" \
    > /etc/apt/sources.list.d/google-cloud-sdk.list

  apt-get update
  apt-get install -y google-cloud-cli
fi

usermod -aG docker ubuntu || true

# Format the RabbitMQ disk only if it has no filesystem yet — this script
# re-runs on every boot, and mkfs here would wipe queue data on a restart.
RABBITMQ_DISK=/dev/disk/by-id/google-rabbitmq-data
if [ -e "$RABBITMQ_DISK" ] && ! blkid "$RABBITMQ_DISK" >/dev/null 2>&1; then
  mkfs.ext4 -m 0 -F "$RABBITMQ_DISK"
fi

mkdir -p /mnt/rabbitmq-data
if [ -e "$RABBITMQ_DISK" ] && ! mountpoint -q /mnt/rabbitmq-data; then
  mount -o discard,defaults "$RABBITMQ_DISK" /mnt/rabbitmq-data
fi
grep -q rabbitmq-data /etc/fstab || \
  echo "$RABBITMQ_DISK /mnt/rabbitmq-data ext4 discard,defaults,nofail 0 2" >> /etc/fstab

chown -R 999:999 /mnt/rabbitmq-data
