# Execution role: permissions ECS itself needs to *run* the task - pull the
# image (auth'd via the Docker Hub secret below) and ship logs to CloudWatch.
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "epita-cloud-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# The managed policy above covers ECR pulls and CloudWatch Logs, but not
# reading the Docker Hub credentials secret used for repositoryCredentials.
resource "aws_iam_role_policy" "ecs_task_execution_secrets" {
  name = "epita-cloud-ecs-execution-secrets"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = [aws_secretsmanager_secret.dockerhub_credentials.arn]
    }]
  })
}

# Task role: permissions the *application* needs at runtime. This is what the
# EC2 instance profile used to grant; now it's scoped to just this ECS task
# instead of to the whole instance.
resource "aws_iam_role" "ecs_task_role" {
  name = "epita-cloud-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "dynamodb_access" {
  name = "epita-cloud-dynamodb-access"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "dynamodb:PutItem", "dynamodb:GetItem",
        "dynamodb:Scan", "dynamodb:Query",
        "dynamodb:UpdateItem", "dynamodb:DeleteItem"
      ]
      Resource = [
        aws_dynamodb_table.events.arn,
        aws_dynamodb_table.rooms.arn
      ]
    }]
  })
}
