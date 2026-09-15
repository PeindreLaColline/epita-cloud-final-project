resource "tls_private_key" "ssh_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "deployer" {
  key_name   = "epita-cloud-deployer-key"
  public_key = tls_private_key.ssh_key.public_key_openssh
}

output "ec2_private_key" {
  value     = tls_private_key.ssh_key.private_key_pem
  sensitive = true
}