resource "google_sql_database_instance" "mysql" {
  name             = "entri-mysql"
  database_version = "MYSQL_8_0"
  region           = var.region

  settings {
    tier = var.tier

    backup_configuration {
      enabled    = true
      start_time = "03:00"
    }

    ip_configuration {
      ipv4_enabled = true
      ssl_mode     = "ENCRYPTED_ONLY"

      authorized_networks {
        name  = "entri-vm"
        value = var.authorized_vm_ip
      }
    }
  }

  deletion_protection = true
}

resource "google_sql_database" "entri" {
  name     = var.database_name
  instance = google_sql_database_instance.mysql.name
}

resource "google_sql_user" "app" {
  name     = var.db_user
  instance = google_sql_database_instance.mysql.name
  password = var.db_password
}
