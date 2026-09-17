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

data "aws_availability_zones" "available" {
  state = "available"
}

# Security group in front of the ALB - open to the internet on port 80
module "alb-security-group" {
  source       = "./security-group"
  vpc_id       = aws_vpc.vpc.id
  ingressrules = [80]
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

# Two public subnets in two different AZs so the ASG/ALB can spread across them
resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
}

resource "aws_subnet" "public_subnet_2" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = data.aws_availability_zones.available.names[1]
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

resource "aws_route_table_association" "public_rta_2" {
  subnet_id      = aws_subnet.public_subnet_2.id
  route_table_id = aws_route_table.public_rt.id
}

# Instance security group: only the ALB can reach the app port; SSH stays open for debugging
resource "aws_security_group" "instance_sg" {
  name        = "epita-cloud-instance-sg"
  description = "Allow app traffic from the ALB only, plus SSH for debugging"
  vpc_id      = aws_vpc.vpc.id

  ingress {
    description     = "App traffic from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [module.alb-security-group.sg_output]
  }

  ingress {
    description = "SSH for debugging"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Instances are self-sufficient at boot: install docker, pull the latest image, run it.
# A new deploy is rolled out with an ASG instance refresh (see deploy.yml), not SSH.
resource "aws_launch_template" "app_lt" {
  name_prefix   = "epita-cloud-lt-"
  image_id      = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name

  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_profile.name
  }

  network_interfaces {
    associate_public_ip_address = true
    security_groups             = [aws_security_group.instance_sg.id]
  }

  user_data = base64encode(<<-USERDATA
              #!/bin/bash
              yum update -y
              yum install -y docker
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ec2-user
              docker pull ${var.docker_image}
              docker run -d \
                --name epita-cloud \
                --restart unless-stopped \
                -p 8080:8080 \
                -e AWS_REGION=eu-west-3 \
                -e COGNITO_ISSUER_URI="https://cognito-idp.eu-west-3.amazonaws.com/${aws_cognito_user_pool.main.id}" \
                ${var.docker_image}
              USERDATA
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = var.ec2_name
    }
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb" "app_alb" {
  name               = "epita-cloud-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [module.alb-security-group.sg_output]
  subnets            = [aws_subnet.public_subnet.id, aws_subnet.public_subnet_2.id]
}

resource "aws_lb_target_group" "app_tg" {
  name        = "epita-cloud-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.vpc.id
  target_type = "instance"

  health_check {
    path                = "/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 30
    timeout             = 5
    matcher             = "200"
  }
}

resource "aws_lb_listener" "app_listener" {
  load_balancer_arn = aws_lb.app_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app_tg.arn
  }
}

# Scalability + high availability: 2 instances minimum, spread across 2 AZs,
# behind the ALB, able to grow to 3 under load.
resource "aws_autoscaling_group" "app_asg" {
  name                = "epita-cloud-asg"
  min_size            = 2
  max_size            = 3
  desired_capacity    = 2
  vpc_zone_identifier = [aws_subnet.public_subnet.id, aws_subnet.public_subnet_2.id]
  target_group_arns   = [aws_lb_target_group.app_tg.arn]

  health_check_type         = "ELB"
  health_check_grace_period = 90

  launch_template {
    id      = aws_launch_template.app_lt.id
    version = "$Latest"
  }

  instance_refresh {
    strategy = "Rolling"
    preferences {
      min_healthy_percentage = 50
      instance_warmup        = 90
    }
  }

  tag {
    key                 = "Name"
    value               = var.ec2_name
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_policy" "cpu_scaling" {
  name                   = "epita-cloud-cpu-target-tracking"
  autoscaling_group_name = aws_autoscaling_group.app_asg.name
  policy_type             = "TargetTrackingScaling"

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }
    target_value = 60
  }
}

output "alb_dns_name" {
  value = aws_lb.app_alb.dns_name
}
