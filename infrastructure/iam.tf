resource "aws_iam_role" "ec2_dynamodb_role" {
  name = "epita-cloud-ec2-dynamodb-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "dynamodb_access" {
  name = "epita-cloud-dynamodb-access"
  role = aws_iam_role.ec2_dynamodb_role.id

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
        aws_dynamodb_table.users.arn
      ]
    }]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "epita-cloud-ec2-profile"
  role = aws_iam_role.ec2_dynamodb_role.name
}