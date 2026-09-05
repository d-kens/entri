output "vm_ip" {
  value = module.compute.vm_static_ip
}

output "vm_service_account_email" {
  value = module.compute.vm_service_account_email
}

output "db_instance_connection_name" {
  value = module.database.instance_connection_name
}

output "db_public_ip" {
  value = module.database.public_ip
}
