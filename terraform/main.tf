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
    dynamodb_table = "terraform-lock"
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


data "aws_eks_cluster" "cluster" {
  #name = data.terraform_remote_state.core.outputs.eks_cluster_name
  name = "tech-challenge-eks"
}

data "aws_eks_cluster_auth" "cluster" {
  #name = data.terraform_remote_state.core.outputs.eks_cluster_name
  name = "tech-challenge-eks"
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
    DB_HOST               = data.terraform_remote_state.database.outputs.rds_endpoint
    DB_PORT               = "5432"
    DB_NAME               = "techchallenge"
    DB_USER               = "postgres"
    AWS_REGION            = "us-east-1"
    COGNITO_USER_POOL_ID  = data.terraform_remote_state.core.outputs.cognito_user_pool_id
    COGNITO_CLIENT_ID     = data.terraform_remote_state.core.outputs.cognito_user_pool_client_id
  }
}

resource "kubernetes_secret" "app_secrets" {
  metadata {
    name      = "app-secrets"
    namespace = kubernetes_namespace.tech_challenge.metadata[0].name
  }

  data = {
    # -------------------------------
    # REMOVIDO: banco (comentado)
    # -------------------------------
    # DB_PASSWORD = var.db_password
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
      port        = 80
      target_port = 8080
    }

    type = "NodePort"
  }
}
