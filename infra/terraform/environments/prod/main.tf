module "network" {
  source = "../../modules/network"

  admin_ssh_cidr = var.admin_ssh_cidr
}

module "compute" {
  source = "../../modules/compute"

  project_id        = var.project_id
  region            = var.region
  zone              = var.zone
  network_tag       = module.network.network_tag
  ssh_public_key    = var.ssh_public_key
  ci_ssh_public_key = var.ci_ssh_public_key
}

module "database" {
  source = "../../modules/database"

  region           = var.region
  db_user          = var.db_user
  db_password      = var.db_password
  authorized_vm_ip = "${module.compute.vm_static_ip}/32"
}
