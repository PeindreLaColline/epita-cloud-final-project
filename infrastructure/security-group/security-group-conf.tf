variable "vpc_id" {
  type = string
}

variable "ingressrules" {
  type    = list(number)
  default = [80, 443]
}

variable "egressrules" {
  type    = list(number)
  default = [80, 443]
}

variable "name" {
  type        = string
  description = "Unique security group name. Must not collide with any other SG in the VPC."
  default     = "security_group_web"
}

resource "aws_security_group" "sg" {
  name        = var.name
  description = "Allow HTTPS, HTTP"

  vpc_id = var.vpc_id

  dynamic "ingress" {
    iterator = port
    for_each = var.ingressrules
    content {
      from_port   = port.value
      to_port     = port.value
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

output "sg_output" {
  value = aws_security_group.sg.id
}
