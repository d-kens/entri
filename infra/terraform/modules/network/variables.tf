variable "admin_ssh_cidr" {
  description = "CIDR allowed to SSH into the VM (your IP, as x.x.x.x/32)"
  type        = string
}

variable "network_tag" {
  description = "Network tag applied to the VM and matched by these firewall rules"
  type        = string
  default     = "entri-vm"
}
