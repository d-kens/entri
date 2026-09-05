terraform {
  backend "gcs" {
    bucket = "entri-prod-tfstate"
    prefix = "prod"
  }
}
