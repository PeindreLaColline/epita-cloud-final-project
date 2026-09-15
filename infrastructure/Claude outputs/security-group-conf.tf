variable "vpc_id" {
  type = string
}

variable "ingressrules" {
  type    = list(number)
  default = [80, 443, 8080]
}

variable "egressrules" {
  type    = list(number)
  default = [80, 443]
}

resource "aws_security_group" "sg" {
  name        = "security_group_web"
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

  dynamic "egress" {
    iterator = port
    for_each = var.egressrules
    content {
      from_port   = port.value
      to_port     = port.value
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }
}

output "sg_output" {
  value = aws_security_group.sg.id
}