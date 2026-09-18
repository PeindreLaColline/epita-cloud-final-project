# Daily point-in-time export of both DynamoDB tables to S3 (disaster recovery / offsite backup).
# Requires point_in_time_recovery enabled on the source tables (see dynamodb.tf).

# The bucket already exists (created outside this Terraform config); adopt it into
# state instead of trying to create it. No-op once it has been imported.
import {
  to = aws_s3_bucket.dynamodb_backups
  id = "backups-dr-363191779144-eu-west-3-an"
}

resource "aws_s3_bucket" "dynamodb_backups" {
  bucket        = "backups-dr-363191779144-eu-west-3-an"
  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "dynamodb_backups" {
  bucket                  = aws_s3_bucket.dynamodb_backups.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "dynamodb_backups" {
  bucket = aws_s3_bucket.dynamodb_backups.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "dynamodb_backups" {
  bucket = aws_s3_bucket.dynamodb_backups.id

  rule {
    id     = "expire-old-exports"
    status = "Enabled"

    filter {}

    expiration {
      days = 30
    }
  }
}

data "archive_file" "backup_lambda" {
  type        = "zip"
  source_file = "${path.module}/lambda/backup/index.py"
  output_path = "${path.module}/lambda/backup.zip"
}

resource "aws_iam_role" "backup_lambda_role" {
  name = "epita-cloud-backup-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "backup_lambda_policy" {
  name = "epita-cloud-backup-lambda-policy"
  role = aws_iam_role.backup_lambda_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:ExportTableToPointInTime",
          "dynamodb:DescribeTable",
          "dynamodb:DescribeContinuousBackups"
        ]
        Resource = [
          aws_dynamodb_table.events.arn,
          aws_dynamodb_table.rooms.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:AbortMultipartUpload",
          "s3:GetBucketLocation"
        ]
        Resource = [
          aws_s3_bucket.dynamodb_backups.arn,
          "${aws_s3_bucket.dynamodb_backups.arn}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:eu-west-3:363191779144:*"
      }
    ]
  })
}

resource "aws_lambda_function" "dynamodb_backup" {
  function_name    = "epita-cloud-dynamodb-backup"
  role             = aws_iam_role.backup_lambda_role.arn
  handler          = "index.handler"
  runtime          = "python3.12"
  timeout          = 30
  filename         = data.archive_file.backup_lambda.output_path
  source_code_hash = data.archive_file.backup_lambda.output_base64sha256

  environment {
    variables = {
      TABLE_ARNS    = "${aws_dynamodb_table.events.arn},${aws_dynamodb_table.rooms.arn}"
      BACKUP_BUCKET = aws_s3_bucket.dynamodb_backups.bucket
    }
  }
}

resource "aws_cloudwatch_event_rule" "daily_backup" {
  name                = "epita-cloud-daily-dynamodb-backup"
  schedule_expression = "rate(1 day)"
}

resource "aws_cloudwatch_event_target" "daily_backup" {
  rule = aws_cloudwatch_event_rule.daily_backup.name
  arn  = aws_lambda_function.dynamodb_backup.arn
}

resource "aws_lambda_permission" "allow_eventbridge" {
  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.dynamodb_backup.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.daily_backup.arn
}

output "dynamodb_backup_bucket" {
  value = aws_s3_bucket.dynamodb_backups.bucket
}
