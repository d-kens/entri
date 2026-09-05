output "vm_static_ip" {
  value = google_compute_address.vm_static_ip.address
}

output "vm_service_account_email" {
  value = google_service_account.vm.email
}

output "vm_name" {
  value = google_compute_instance.vm.name
}

output "vm_zone" {
  value = google_compute_instance.vm.zone
}

output "rabbitmq_disk_name" {
  value = google_compute_disk.rabbitmq_data.name
}
