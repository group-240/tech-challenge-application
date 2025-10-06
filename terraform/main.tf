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
    # API_GATEWAY_URL não é necessário na aplicação - apenas para referência externa
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

  # Não esperar por rollout - permite Terraform completar mesmo se pods não ficarem ready
  # Útil quando imagem ainda não existe no ECR ou durante troubleshooting
  wait_for_rollout = false

  spec {
    replicas = 1  # Reduzido para 1 réplica (node t3.small tem apenas 2GB)

    # Estratégia de rolling update para evitar 2 pods simultâneos
    strategy {
      type = "RollingUpdate"
      rolling_update {
        max_surge       = 0  # Não criar pod extra durante update
        max_unavailable = 1  # Pode ficar indisponível durante update
      }
    }

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

          # Variáveis de ambiente para otimizar JVM
          env {
            name  = "JAVA_OPTS"
            value = "-Xms256m -Xmx768m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
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
            initial_delay_seconds = 90   # Aumentado para dar tempo da JVM iniciar
            period_seconds        = 30
            timeout_seconds       = 5
            failure_threshold     = 3
          }

          readiness_probe {
            http_get {
              path = "/api/health"
              port = 8080
            }
            initial_delay_seconds = 60   # Aumentado para aplicação Spring Boot
            period_seconds        = 10
            timeout_seconds       = 3
            failure_threshold     = 3
          }

          resources {
            requests = {
              memory = "512Mi"  # Reduzido para caber no t3.small (2GB node)
              cpu    = "150m"   # CPU mínima para Spring Boot
            }
            limits = {
              memory = "1Gi"    # Limite de 1GB (deixa ~1GB para sistema)
              cpu    = "500m"   # CPU suficiente para operação
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
    
    # SOLUÇÃO: Annotations do AWS Load Balancer Controller
    # Deixa o controller gerenciar automaticamente o Target Group Binding
    annotations = {
      "service.beta.kubernetes.io/aws-load-balancer-type"              = "external"
      "service.beta.kubernetes.io/aws-load-balancer-nlb-target-type"   = "ip"
      "service.beta.kubernetes.io/aws-load-balancer-scheme"            = "internal"
      "service.beta.kubernetes.io/aws-load-balancer-target-group-arn"  = data.terraform_remote_state.core.outputs.target_group_arn
    }
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

    type = "LoadBalancer" # Muda para LoadBalancer para o controller processar annotations
  }
}

# REMOVIDO: TargetGroupBinding manual (agora gerenciado pelo controller via annotations)
# O AWS Load Balancer Controller irá criar automaticamente baseado nas annotations acima
# resource "kubernetes_manifest" "target_group_binding" { ... }

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