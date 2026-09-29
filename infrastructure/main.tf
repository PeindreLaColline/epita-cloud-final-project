provider "aws" {
  region = "eu-west-3"
}

data "aws_availability_zones" "available" {
  state = "available"
}

# Security group in front of the ALB - open to the internet on port 80
module "alb-security-group" {
  source       = "./security-group"
  vpc_id       = aws_vpc.vpc.id
  ingressrules = [80]
  name         = "epita-cloud-alb-sg"
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

# Two public subnets in two different AZs so the ALB/ECS tasks can spread across them
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

# Task security group: only the ALB can reach the app port. No SSH rule here -
# there's no instance to SSH into anymore; container logs go to CloudWatch Logs.
resource "aws_security_group" "ecs_task_sg" {
  name        = "epita-cloud-task-sg"
  description = "Allow app traffic from the ALB only"
  vpc_id      = aws_vpc.vpc.id

  ingress {
    description     = "App traffic from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [module.alb-security-group.sg_output]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
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
  # name_prefix (max 6 chars for target groups), not a fixed name: paired with
  # create_before_destroy below so a forced replacement (e.g. target_type
  # instance -> ip) creates the new target group and repoints the listener
  # before the old target group is destroyed, instead of trying to delete a
  # target group that's still attached to a listener.
  name_prefix = "epcld-"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.vpc.id
  target_type = "ip" # ECS/Fargate tasks register by IP, not instance ID

  health_check {
    path                = "/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 30
    timeout             = 5
    matcher             = "200"
  }

  lifecycle {
    create_before_destroy = true
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

# Docker Hub is a private repo, so ECS needs credentials to pull the image.
# Kept in Secrets Manager and referenced from the task definition, instead of
# being embedded in plaintext in a user_data script like the EC2 setup did.
resource "aws_secretsmanager_secret" "dockerhub_credentials" {
  name = "epita-cloud-dockerhub-credentials"
}

resource "aws_secretsmanager_secret_version" "dockerhub_credentials" {
  secret_id = aws_secretsmanager_secret.dockerhub_credentials.id
  secret_string = jsonencode({
    username = var.dockerhub_username
    password = var.dockerhub_token
  })
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/epita-cloud"
  retention_in_days = 14
}

resource "aws_ecs_cluster" "main" {
  name = "epita-cloud-cluster"
}

resource "aws_ecs_task_definition" "app" {
  family                   = "epita-cloud"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "epita-cloud"
      image     = var.docker_image
      essential = true

      repositoryCredentials = {
        credentialsParameter = aws_secretsmanager_secret.dockerhub_credentials.arn
      }

      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]

      environment = [
        { name = "AWS_REGION", value = "eu-west-3" },
        { name = "COGNITO_ISSUER_URI", value = "https://cognito-idp.eu-west-3.amazonaws.com/${aws_cognito_user_pool.main.id}" }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = "eu-west-3"
          "awslogs-stream-prefix" = "app"
        }
      }
    }
  ])
}

# Scalability + high availability: 2 tasks minimum, spread across 2 AZs
# (Fargate schedules across the subnets below), behind the ALB, able to grow
# to 3 under load - the direct replacement for the old ASG min2/max3 policy.
#
# Terraform won't register a new task definition revision just because the
# image tag string (always ":latest") didn't change, so deploy.yml explicitly
# calls `aws ecs update-service --force-new-deployment` on every deploy - the
# same role the `aws autoscaling start-instance-refresh` call used to play.
resource "aws_ecs_service" "app" {
  name            = "epita-cloud-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_subnet.id, aws_subnet.public_subnet_2.id]
    security_groups  = [aws_security_group.ecs_task_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app_tg.arn
    container_name    = "epita-cloud"
    container_port    = 8080
  }

  health_check_grace_period_seconds = 90

  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent         = 200

  depends_on = [aws_lb_listener.app_listener]
}

resource "aws_appautoscaling_target" "app_scaling" {
  max_capacity       = 3
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu_scaling" {
  name               = "epita-cloud-cpu-target-tracking"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.app_scaling.resource_id
  scalable_dimension = aws_appautoscaling_target.app_scaling.scalable_dimension
  service_namespace  = aws_appautoscaling_target.app_scaling.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 60
  }
}

output "alb_dns_name" {
  value = aws_lb.app_alb.dns_name
}
