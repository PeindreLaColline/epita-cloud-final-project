provider "aws" {
  region = "eu-west-3"
}

variable "ec2_name" {
  type    = string
  default = "backend-server"
}

data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

module "security-group-module" {
  source       = "./security-group"
  vpc_id       = aws_vpc.vpc.id
  ingressrules = [22, 80, 443, 8080]
}

resource "aws_vpc" "vpc" {
  cidr_block = "10.0.0.0/16"
  tags = {
    Name = "epita-cloud-final-project"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id
}

resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
}

resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
}

resource "aws_route_table_association" "public_rta" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_rt.id
}

resource "aws_instance" "web_server" {
  ami                    = data.aws_ami.amazon_linux_2.id
  instance_type          = "t3.micro"
  subnet_id              = aws_subnet.public_subnet.id
  vpc_security_group_ids = [module.security-group-module.sg_output]
  key_name               = aws_key_pair.deployer.key_name
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  user_data = <<-USERDATA
              #!/bin/bash
              yum update -y
              yum install -y docker
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ec2-user
              USERDATA

  tags = {
    Name = var.ec2_name
  }
}

output "public_ip" {
  value = aws_instance.web_server.public_ip
}
