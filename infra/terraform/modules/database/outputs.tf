output "instance_connection_name" {
  value = google_sql_database_instance.mysql.connection_name
}

output "public_ip" {
  value = google_sql_database_instance.mysql.public_ip_address
}

output "database_name" {
  value = google_sql_database.entri.name
}
