resource "aws_cognito_user_pool" "main" {
  name = "epita-cloud-user-pool"

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = false
    require_uppercase = true
  }

  auto_verified_attributes = ["email"]
}

resource "aws_cognito_user_pool_client" "main" {
  name         = "epita-cloud-client"
  user_pool_id = aws_cognito_user_pool.main.id

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]

  generate_secret = false
}

resource "aws_cognito_user_group" "admin" {
  name         = "ADMIN"
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "Full access - can register events and manage everything"
  precedence   = 0
}

resource "aws_cognito_user_group" "user" {
  name         = "USER"
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "Read-only access to events, stats, and room data"
  precedence   = 1
}

output "cognito_user_pool_id" {
  value = aws_cognito_user_pool.main.id
}

output "cognito_user_pool_client_id" {
  value = aws_cognito_user_pool_client.main.id
}

output "cognito_issuer_uri" {
  value = "https://cognito-idp.eu-west-3.amazonaws.com/${aws_cognito_user_pool.main.id}"
}
