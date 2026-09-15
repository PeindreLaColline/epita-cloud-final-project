resource "aws_dynamodb_table" "events" {
  name         = "events"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "building"
  range_key    = "timestamp"

  attribute {
    name = "building"
    type = "S"
  }
  attribute {
    name = "timestamp"
    type = "S"
  }
}

resource "aws_dynamodb_table" "users" {
  name         = "users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "user_id"

  attribute {
    name = "user_id"
    type = "S"
  }
}