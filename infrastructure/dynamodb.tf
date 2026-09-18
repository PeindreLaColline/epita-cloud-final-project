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

  point_in_time_recovery {
    enabled = true
  }
}

resource "aws_dynamodb_table" "rooms" {
  name         = "rooms"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "room_id"

  attribute {
    name = "room_id"
    type = "S"
  }

  point_in_time_recovery {
    enabled = true
  }
}
