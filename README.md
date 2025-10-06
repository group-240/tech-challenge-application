# 🍔 Tech Challenge - Food Service API

[![Java Version](https://img.shields.io/badge/Java-17-%23ED8B00?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-%236DB33F?logo=spring)](https://spring.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14.9-%23316192?logo=postgresql)](https://www.postgresql.org/)
[![AWS EKS](https://img.shields.io/badge/AWS-EKS-%23FF9900?logo=amazon-aws)](https://aws.amazon.com/eks/)
[![Terraform](https://img.shields.io/badge/Terraform-1.5.0-%23623CE4?logo=terraform)](https://www.terraform.io/)

Aplicação Spring Boot com **Clean Architecture** rodando em **AWS EKS** com deploy automático via **GitHub Actions** e **Terraform**.
---

## 🏗️ Infraestrutura

### Recursos Terraform

- **Kubernetes Namespace** (tech-challenge)
- **ConfigMap** com configurações da aplicação
- **Secret** com dados sensíveis
- **Deployment** com 2 réplicas
- **Service** para exposição interna
- **Health Checks** configurados

### Deploy Automático

```bash
# Workflow GitHub Actions:
1. Maven test + Docker build
2. Push para ECR
3. Terraform apply → Deploy no EKS
```

## 🔐 Autenticação e Segurança

### Fluxo de Autenticação
1. **Cliente registra** via `/api/customers` (cria no Cognito automaticamente)
2. **Autentica** via `/auth` com CPF (Lambda + Cognito)
3. **Recebe JWT** válido por 1 hora
4. **Usa JWT** em endpoints protegidos

### Secrets Gerenciados
```
GitHub Secrets → Terraform → Kubernetes Secrets → Pods
```

## 🍔 API Endpoints

### Públicos (Sem autenticação)
- `GET  /api/health`          - Status da aplicação
- `GET  /api/categories`      - Listar categorias
- `POST /api/categories`      - Criar categoria
- `GET  /api/products`        - Listar produtos
- `POST /api/products`        - Criar produto
- `POST /api/customers`       - Registrar cliente
- `POST /api/webhook/payment` - Webhook Mercado Pago

### Protegidos (Requer JWT)
- `GET  /api/customers`          - Listar clientes
- `POST /api/orders`             - Criar pedido
- `GET  /api/orders`             - Listar pedidos
- `PUT  /api/orders/{id}/status` - Atualizar status

## 🏛️ Clean Architecture

```
src/main/java/com/fiap/techchallenge/
├── domain/                    # Entidades e regras de negócio
│   ├── entities/             # Customer, Order, Product, Category
│   ├── repositories/         # Interfaces de repositório
│   └── exception/           # Exceções de domínio
├── application/              # Casos de uso
│   ├── usecases/            # Lógica de aplicação
│   └── config/              # Configuração de beans
├── adapters/                 # Interface adapters
│   ├── controllers/         # Orquestração
│   ├── gateway/             # Implementação de repositórios
│   └── presenters/          # Formatação de saída
└── external/                 # Frameworks e drivers
    ├── api/                 # REST Controllers
    ├── datasource/          # JPA + PostgreSQL
    └── cognito/             # Integração AWS Cognito
```

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem principal
- **Spring Boot 3.3.5** - Framework web
- **Spring Data JPA** - Persistência
- **Spring Security OAuth2** - Autenticação JWT
- **PostgreSQL 14.9** - Banco de dados
- **Maven** - Gerenciamento de dependências

### AWS Services
- **EKS** - Kubernetes gerenciado
- **ECR** - Registry de containers
- **RDS PostgreSQL** - Banco de dados
- **Cognito** - Autenticação JWT
- **CloudWatch** - Logs e métricas

### DevOps
- **Docker** - Containerização
- **Kubernetes** - Orquestração
- **Terraform** - Infrastructure as Code
- **GitHub Actions** - CI/CD

## 📁 Estrutura

```
.
├── src/                       # Código fonte Java
├── terraform/                 # Infraestrutura EKS
│   ├── main.tf               # Recursos Kubernetes
│   └── variables.tf          # Variáveis
├── postman/                   # Collection API
├── docs/                      # Documentação
├── .github/workflows/         # Pipeline CI/CD
├── Dockerfile                 # Container
└── pom.xml                   # Build Maven
```

## 🔗 Dependências

Este módulo depende dos outputs de:

**tech-challenge-infra-core:**
- `eks_cluster_name`
- `cognito_user_pool_id`
- `cognito_user_pool_client_id`

**tech-challenge-infra-database:**
- `rds_endpoint`

## 📊 Monitoramento

### Health Checks
- **Aplicação:** `https://api.tech-challenge.com/api/health`
- **Métricas:** `https://api.tech-challenge.com/api/actuator/prometheus`

### AWS CloudWatch
- Logs de aplicação e pods
- Métricas de CPU, memória e rede
- Alertas automáticos

## 📚 Documentação Completa

### Arquitetura e Desenvolvimento
- **[📋 GUIA_ARQUITETURA.md](./docs/GUIA_ARQUITETURA.md)** - Clean Architecture explicada
- **[🚀 GUIA_NEGOCIO.md](./docs/GUIA_NEGOCIO.md)** - Regras de negócio do sistema
- **[🔧 GUIA_DESENVOLVIMENTO.md](./docs/GUIA_DESENVOLVIMENTO.md)** - Como modificar o código

### Deploy e Produção
- **[🚀 GUIA_AWS_DEPLOY.md](./docs/GUIA_AWS_DEPLOY.md)** - Deploy completo na AWS
- **[📱 TUTORIAL_API.md](./docs/TUTORIAL_API.md)** - Como usar a API em produção
- **[🔐 SOLUCAO_GITHUB_SECRETS.md](./docs/SOLUCAO_GITHUB_SECRETS.md)** - Gerenciamento de secrets
- **[📊 GUIA_OBSERVABILIDADE.md](./docs/GUIA_OBSERVABILIDADE.md)** - Logs e monitoramento completo

### API Interativa
- **Swagger UI:** `https://api.tech-challenge.com/api/swagger-ui/index.html`
- **Postman Collection:** `postman/Tech_Challenge_API.postman_collection.json`

## 🚀 Quick Start

### 1. Configure GitHub Secrets
```bash
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
DB_PASSWORD=MinhaSenh@123!
JWT_SECRET=meu-jwt-super-seguro
```

### 2. Deploy Automático
```bash
git push origin main  # Deploy via GitHub Actions
```

### 3. Teste a API
```bash
# Health check
curl https://api.tech-challenge.com/api/health

# Registrar cliente
curl -X POST https://api.tech-challenge.com/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@email.com","cpf":"12345678901"}'
```

---

## 📄 Licença

MIT License - veja [LICENSE](../LICENSE.md) para detalhes.

**Desenvolvido com ❤️ pela equipe FIAP Tech Challenge**