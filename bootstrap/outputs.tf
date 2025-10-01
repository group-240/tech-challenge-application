output "s3_bucket_name" {
  description = "Nome do bucket S3 criado para o Terraform state"
  value       = aws_s3_bucket.terraform_state.bucket
}

output "s3_bucket_arn" {
  description = "ARN do bucket S3"
  value       = aws_s3_bucket.terraform_state.arn
}

output "dynamodb_table_name" {
  description = "Nome da tabela DynamoDB para lock"
  value       = aws_dynamodb_table.terraform_lock.name
}

output "dynamodb_table_arn" {
  description = "ARN da tabela DynamoDB"
  value       = aws_dynamodb_table.terraform_lock.arn
}

# Configuração do backend para copiar no main.tf
output "backend_config" {
  description = "Configuração do backend para usar no main.tf"
  value = {
    bucket         = aws_s3_bucket.terraform_state.bucket
    key            = "core/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = aws_dynamodb_table.terraform_lock.name
    encrypt        = true
  }
}