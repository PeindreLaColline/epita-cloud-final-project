terraform {
  backend "s3" {
    bucket = "epita-cloud-final-project-terraform-363191779144-eu-west-3-an"
    key    = "infrastructure/terraform.tfstate"
    region = "eu-west-3"
  }
}
