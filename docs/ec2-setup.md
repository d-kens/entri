# EC2 Deployment Setup

This document covers the full process of deploying Oro on an AWS EC2 instance, including the reasoning behind each step.

---

## Phase 1 — AWS Setup

### 1. Launch an EC2 Instance

Go to the AWS console → EC2 → **Launch instance**.

| Setting | Value | Why |
|---|---|---|
| AMI | Ubuntu 22.04 LTS | Stable, well supported, long term support until 2027 |
| Instance type | t3.small | The Spring Boot JVM needs at least 1GB headroom. t3.micro (1GB total) will OOM kill the database under load |
| Storage | 20 GB | Default 8GB is too tight once Docker images and logs accumulate |
| Key pair | Create new — `oro-key.pem` | Required to SSH into the instance. Download and store it safely — AWS will not let you download it again |

**Security group rules:**

| Port | Source | Why |
|---|---|---|
| 22 | Your IP only | SSH access — restricting to your IP prevents brute force attempts from the internet |
| 80 | 0.0.0.0/0 | HTTP — needed for the HTTPS redirect and Let's Encrypt certificate renewal challenges |
| 443 | 0.0.0.0/0 | HTTPS — the main entry point for all app traffic |

---

### 2. Allocate an Elastic IP

By default, EC2 gives your instance a public IP that changes every time the instance is stopped and started. This would break your DNS every time you reboot.

An Elastic IP is a static IP permanently assigned to your AWS account until you release it.

1. EC2 → **Elastic IPs** → **Allocate Elastic IP address**
2. Select the newly allocated IP → **Actions** → **Associate Elastic IP address**
3. Select your instance and click **Associate**

Note down the IP — you'll use it in DNS and for SSH.

---

### 3. Point the Domain to the EC2 IP

In your DNS provider, update the A record:

| Field | Value |
|---|---|
| Type | A |
| Name | @ |
| Value | Your Elastic IP |
| TTL | 300 |

DNS propagation can take anywhere from a few minutes to a few hours. Verify with:

```bash
dig YOUR_DOMAIN +short
```

When it returns your Elastic IP, propagation is complete.

---

## Phase 2 — Server Setup

### 4. SSH into the Instance

The key file permissions must be restricted — SSH will refuse to use a key that is readable by others:
Lik
```bash
chmod 400 ~/Downloads/oro-key.pem
ssh -i ~/Downloads/oro-key.pem ubuntu@<your-elastic-ip>
```

---

### 5. Install Docker and Certbot

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y ca-certificates curl certbot
```

Add Docker's official apt repository:

```bash
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu jammy stable" | sudo tee /etc/apt/sources.list.d/docker.list
```

> **Note:** Use `jammy` (Ubuntu 22.04 codename) hardcoded rather than relying on `$VERSION_CODENAME` — the variable can resolve incorrectly on some Ubuntu builds and produce a malformed apt source entry.

```bash
sudo apt update && sudo apt install -y docker-ce docker-ce-cli docker-compose-plugin
```

Allow running Docker without `sudo`:

```bash
sudo usermod -aG docker ubuntu
newgrp docker
```

Without this, every `docker` command would need `sudo`, and `docker compose` would run as root with different environment variables.

---

### 6. Get the SSL Certificate

At this point nothing is running on port 80, so certbot can spin up its own temporary web server to complete the domain ownership challenge:

```bash
sudo certbot certonly --standalone -d YOUR_DOMAIN
```

Let's Encrypt verifies you own the domain by making an HTTP request to `http://YOUR_DOMAIN/.well-known/acme-challenge/<token>`. Once verified, it issues a 90-day certificate and saves it to:

```
/etc/letsencrypt/live/YOUR_DOMAIN/fullchain.pem
/etc/letsencrypt/live/YOUR_DOMAIN/privkey.pem
```

Certbot also sets up automatic renewal via a systemd timer — you don't need to manually renew.

---

## Phase 3 — App Setup

### 7. Clone the Repository

```bash
git clone https://github.com/d-kens/oro.git
cd oro
```

---

### 8. Create the Environment File

```bash
cp .env.example .env
nano .env
```

Fill in all values. Generate the JWT secret with:

```bash
openssl rand -hex 64
```

Save and exit nano with `Ctrl+X` → `Y` → `Enter`.

---

### 9. Upload the Firebase Service Account

The API needs a Firebase service account JSON to authenticate with Firebase Storage. This file is sensitive and should never be committed to git.

Copy it from your local machine:

```bash
# Run this on your local machine
scp -i ~/Downloads/oro-key.pem ~/Downloads/firebase.json ubuntu@<your-elastic-ip>:/tmp/firebase.json
```

Move it to a secure location on the server:

```bash
sudo mkdir -p /etc/oro
sudo mv /tmp/firebase.json /etc/oro/firebase.json
sudo chmod 600 /etc/oro/firebase.json
```

The `docker-compose.yml` mounts this file into the API container at `/secrets/firebase.json`.

---

### 10. Log in to GHCR

The Docker images are private on GitHub Container Registry. Docker needs to authenticate before it can pull them:

```bash
echo YOUR_GHCR_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

This stores the credentials in `~/.docker/config.json`, which Docker uses automatically for all subsequent pulls.

---

### 11. Start All Containers

```bash
docker compose up -d
```

This pulls all images from GHCR and starts 5 containers:

| Container | Role |
|---|---|
| `oro-db-1` | MySQL database |
| `oro-api-1` | Spring Boot API |
| `oro-web-1` | Angular frontend served by Nginx |
| `oro-nginx-1` | Reverse proxy — terminates SSL and routes traffic |
| `oro-watchtower-1` | Polls GHCR every 5 minutes and auto-updates containers |

Verify all are running:

```bash
docker compose ps
```

---

## Troubleshooting

### Docker apt source malformed entry

Running the `echo "deb ..."` command with `$VERSION_CODENAME` can produce `resolute` instead of `jammy` due to a multiline paste issue. Fix:

```bash
sudo rm /etc/apt/sources.list.d/docker.list
echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu jammy stable" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt update && sudo apt install -y docker-ce docker-ce-cli docker-compose-plugin
```

---

### Database OOM killed (exit code 137)

Exit code 137 means the process was killed by the OS due to insufficient memory. This happens on t3.micro (1GB RAM) which is too small for MySQL + Spring Boot + Nginx running simultaneously.

**Fix:** Upgrade to t3.small (2GB):
1. Stop the instance in AWS console
2. **Actions** → **Instance settings** → **Change instance type** → t3.small
3. Start the instance

Memory limits are also set in `docker-compose.yml` to prevent any single container from starving the others:
- MySQL: 512MB
- API: 768MB

---

### Watchtower failing with "client version 1.25 is too old"

The Watchtower container's Docker client was negotiating API version 1.25, which is below the minimum (1.40) required by the Docker daemon on the EC2 instance.

**Fix:** Set `DOCKER_API_VERSION=1.41` in the Watchtower environment in `docker-compose.yml` to force it to use a compatible API version.
