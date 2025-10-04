variable "db_password" {
  description = "Database password - HARDCODED para ambiente DEV (apenas estudo)"
  type        = string
  sensitive   = true
  default     = "DevPassword123!"
}

variable "jwt_secret" {
  description = "JWT secret key - HARDCODED para ambiente DEV (apenas estudo)"
  type        = string
  sensitive   = true
  default     = "dev-jwt-secret-key-12345"
}

variable "ecr_repository_url" {
  description = "ECR repository URL"
  type        = string
}
