terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }
  backend "s3" {
    bucket         = "tech-challenge-tfstate-533267363894-4"
    key            = "application/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "tech-challenge-terraform-lock-533267363894"
    encrypt        = true
  }
}

provider "aws" {
  region = "us-east-1"
}

data "terraform_remote_state" "core" {
  backend = "s3"
  config = {
    bucket = "tech-challenge-tfstate-533267363894-4"
    key    = "core/terraform.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "database" {
  backend = "s3"
  config = {
    bucket = "tech-challenge-tfstate-533267363894-4"
    key    = "database/terraform.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "gateway" {
  backend = "s3"
  config = {
    bucket = "tech-challenge-tfstate-533267363894-4"
    key    = "gateway/terraform.tfstate"
    region = "us-east-1"
  }
}

data "aws_eks_cluster" "cluster" {
  name = data.terraform_remote_state.core.outputs.eks_cluster_name
}

data "aws_eks_cluster_auth" "cluster" {
  name = data.terraform_remote_state.core.outputs.eks_cluster_name
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.cluster.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.cluster.token
}

resource "kubernetes_namespace" "tech_challenge" {
  metadata {
    name = "tech-challenge"
  }
}

resource "kubernetes_config_map" "app_config" {
  metadata {
    name      = "app-config"
    namespace = kubernetes_namespace.tech_challenge.metadata[0].name
  }

  data = {
    SPRING_PROFILES_ACTIVE = "dev"
    DB_HOST               = data.terraform_remote_state.database.outputs.rds_address
    DB_PORT               = tostring(data.terraform_remote_state.database.outputs.rds_port)
    DB_NAME               = data.terraform_remote_state.database.outputs.rds_db_name
    DB_USER               = "postgres"
    AWS_REGION            = "us-east-1"
    COGNITO_USER_POOL_ID  = data.terraform_remote_state.core.outputs.cognito_user_pool_id
    COGNITO_CLIENT_ID     = data.terraform_remote_state.core.outputs.cognito_user_pool_client_id
    API_GATEWAY_URL       = data.terraform_remote_state.gateway.outputs.api_gateway_invoke_url
  }
}

resource "kubernetes_secret" "app_secrets" {
  metadata {
    name      = "app-secrets"
    namespace = kubernetes_namespace.tech_challenge.metadata[0].name
  }

  data = {
    DB_PASSWORD = var.db_password
    JWT_SECRET  = var.jwt_secret
  }
}

resource "kubernetes_deployment" "tech_challenge_app" {
  metadata {
    name      = "tech-challenge-app"
    namespace = kubernetes_namespace.tech_challenge.metadata[0].name
  }

  spec {
    replicas = 2

    selector {
      match_labels = {
        app = "tech-challenge-app"
      }
    }

    template {
      metadata {
        labels = {
          app = "tech-challenge-app"
        }
      }

      spec {
        container {
          image = "${var.ecr_repository_url}:latest"
          name  = "tech-challenge-app"

          port {
            container_port = 8080
          }

          env_from {
            config_map_ref {
              name = kubernetes_config_map.app_config.metadata[0].name
            }
          }

          env_from {
            secret_ref {
              name = kubernetes_secret.app_secrets.metadata[0].name
            }
          }

          liveness_probe {
            http_get {
              path = "/api/health"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 30
          }

          readiness_probe {
            http_get {
              path = "/api/health"
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }

          resources {
            requests = {
              memory = "512Mi"
              cpu    = "250m"
            }
            limits = {
              memory = "1Gi"
              cpu    = "500m"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "tech_challenge_service" {
  metadata {
    name      = "tech-challenge-service"
    namespace = kubernetes_namespace.tech_challenge.metadata[0].name
  }

  spec {
    selector = {
      app = "tech-challenge-app"
    }

    port {
      name        = "http"
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }

    type = "ClusterIP" # Interno apenas, NLB gerenciado via TargetGroupBinding
  }
}

# TargetGroupBinding - Conecta automaticamente o service ao NLB
# AWS Load Balancer Controller (instalado via Helm no infra-core) gerencia isso
resource "kubernetes_manifest" "target_group_binding" {
  manifest = {
    apiVersion = "elbv2.k8s.aws/v1beta1"
    kind       = "TargetGroupBinding"
    metadata = {
      name      = "tech-challenge-tgb"
      namespace = kubernetes_namespace.tech_challenge.metadata[0].name
    }
    spec = {
      serviceRef = {
        name = kubernetes_service.tech_challenge_service.metadata[0].name
        port = 80
      }
      targetGroupARN = data.terraform_remote_state.core.outputs.target_group_arn
      targetType     = "ip"  # Registra IPs dos pods diretamente
    }
  }

  depends_on = [
    kubernetes_service.tech_challenge_service,
    data.terraform_remote_state.core
  ]
}

# Outputs para integração com API Gateway
output "service_name" {
  description = "Nome do Kubernetes Service"
  value       = kubernetes_service.tech_challenge_service.metadata[0].name
}

output "service_namespace" {
  description = "Namespace do Kubernetes Service"
  value       = kubernetes_namespace.tech_challenge.metadata[0].name
}

output "service_cluster_ip" {
  description = "Cluster IP do service (interno)"
  value       = kubernetes_service.tech_challenge_service.spec[0].cluster_ip
}