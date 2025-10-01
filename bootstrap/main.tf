# ------------------------------------------------------------------
# Bootstrap: Recursos necessários para o backend do Terraform
# Este arquivo cria S3 e DynamoDB ANTES da infraestrutura principal
# ------------------------------------------------------------------

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # SEM backend S3 - usa state local temporariamente
  # Após criar o S3, migraremos o state
}

provider "aws" {
  region = "us-east-1"  # Região fixa para sua conta
}

locals {
  # Configuração específica para conta AWS 533267363894
  account_id  = "533267363894"
  bucket_name = "tech-challenge-tfstate-${local.account_id}-1"
  table_name  = "tech-challenge-terraform-lock-${local.account_id}-1"
  
  common_tags = {
    Environment = "dev"
    Project     = var.project_name
    ManagedBy   = "terraform-bootstrap"
    AccountId   = local.account_id
    Owner       = "aws-learner-lab"
  }
}

# S3 Bucket para armazenar o state do Terraform
resource "aws_s3_bucket" "terraform_state" {
  bucket = local.bucket_name
  
  tags = local.common_tags
}

# Versionamento do bucket (backup automático)
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Criptografia do bucket
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Bloquear acesso público (segurança)
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Lifecycle para gerenciar versões antigas
resource "aws_s3_bucket_lifecycle_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    id     = "terraform_state_lifecycle"
    status = "Enabled"

    filter {}  # Filter vazio aplica a regra a todos os objetos

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }
}

# DynamoDB para lock do Terraform
resource "aws_dynamodb_table" "terraform_lock" {
  name           = local.table_name
  billing_mode   = "PAY_PER_REQUEST"  # Mais econômico para baixo volume
  hash_key       = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = local.common_tags
}

# Política IAM para acesso ao bucket (opcional - para maior segurança)
resource "aws_s3_bucket_policy" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DenyInsecureConnections"
        Effect = "Deny"
        Principal = "*"
        Action = "s3:*"
        Resource = [
          aws_s3_bucket.terraform_state.arn,
          "${aws_s3_bucket.terraform_state.arn}/*"
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      }
    ]
  })
}