resource "google_service_account" "vm" {
  account_id   = "entri-vm"
  display_name = "entri VM (deploy.sh identity)"
}

# Lets deploy.sh read secrets via `gcloud secrets versions access` with no JSON key.
resource "google_project_iam_member" "vm_secret_accessor" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.vm.email}"
}

# Lets deploy.sh pull images via `gcloud auth configure-docker` with no JSON key.
resource "google_project_iam_member" "vm_artifact_reader" {
  project = var.project_id
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:${google_service_account.vm.email}"
}

resource "google_compute_address" "vm_static_ip" {
  name   = "entri-vm-ip"
  region = var.region
}

# Separate, dedicated disk for RabbitMQ data — never shares a disk with anything
# else, so it can be snapshotted and restored independently of the VM's boot disk.
resource "google_compute_disk" "rabbitmq_data" {
  name = "rabbitmq-data"
  zone = var.zone
  type = "pd-balanced"
  size = var.rabbitmq_disk_size_gb
}

resource "google_compute_instance" "vm" {
  name         = "entri-vm"
  machine_type = var.machine_type
  zone         = var.zone
  tags         = [var.network_tag]

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2204-lts"
      size  = var.boot_disk_size_gb
      type  = "pd-balanced"
    }
  }

  attached_disk {
    source      = google_compute_disk.rabbitmq_data.id
    device_name = "rabbitmq-data"
  }

  network_interface {
    network = "default"
    access_config {
      nat_ip = google_compute_address.vm_static_ip.address
    }
  }

  service_account {
    email  = google_service_account.vm.email
    scopes = ["cloud-platform"]
  }

  metadata = {
    ssh-keys       = "${var.ssh_public_key}\n${var.ci_ssh_public_key}"
    startup-script = file("${path.module}/scripts/startup.sh")
  }

  allow_stopping_for_update = true
}
