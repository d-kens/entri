variable "region" {
  type = string
}

variable "tier" {
  description = "Cloud SQL machine tier"
  type        = string
  default     = "db-f1-micro"
}

variable "database_name" {
  type    = string
  default = "entri"
}

variable "db_user" {
  type = string
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "authorized_vm_ip" {
  description = "The VM's static IP, allowed to reach Cloud SQL's public IP (as x.x.x.x/32)"
  type        = string
}
