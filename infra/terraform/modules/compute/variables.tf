variable "project_id" {
  type = string
}

variable "region" {
  type = string
}

variable "zone" {
  type = string
}

variable "machine_type" {
  type    = string
  default = "e2-small"
}

variable "boot_disk_size_gb" {
  type    = number
  default = 20
}

variable "rabbitmq_disk_size_gb" {
  type    = number
  default = 10
}

variable "network_tag" {
  type = string
}

variable "ssh_public_key" {
  description = "Public key added for the 'ubuntu' user, format: 'ubuntu:ssh-ed25519 AAAA... comment'"
  type        = string
}

variable "ci_ssh_public_key" {
  description = "Second public key for CI deploys, same format as ssh_public_key"
  type        = string
}
