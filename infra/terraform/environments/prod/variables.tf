variable "project_id" {
  type    = string
  default = "entri-prod"
}

variable "region" {
  type    = string
  default = "africa-south1"
}

variable "zone" {
  type    = string
  default = "africa-south1-a"
}

variable "domain" {
  type    = string
  default = "oro.co.ke"
}

variable "admin_ssh_cidr" {
  description = "Your current public IP, as x.x.x.x/32 — update this if it changes"
  type        = string
}

variable "ssh_public_key" {
  description = "Format: 'ubuntu:ssh-ed25519 AAAA... comment'"
  type        = string
}

variable "ci_ssh_public_key" {
  description = "Dedicated CI-only key, same format as ssh_public_key"
  type        = string
}

variable "db_user" {
  type    = string
  default = "entri"
}

variable "db_password" {
  type      = string
  sensitive = true
}
