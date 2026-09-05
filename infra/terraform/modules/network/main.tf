resource "google_compute_firewall" "allow_ssh" {
  name          = "entri-allow-ssh"
  network       = "default"
  source_ranges = [var.admin_ssh_cidr]
  target_tags   = [var.network_tag]

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
}

resource "google_compute_firewall" "allow_iap_ssh" {
  name          = "entri-allow-iap-ssh"
  network       = "default"
  source_ranges = ["35.235.240.0/20"] # fixed range IAP tunnels originate from
  target_tags   = [var.network_tag]

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
}

resource "google_compute_firewall" "allow_http_https" {
  name          = "entri-allow-http-https"
  network       = "default"
  source_ranges = ["0.0.0.0/0"]
  target_tags   = [var.network_tag]

  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }
}
