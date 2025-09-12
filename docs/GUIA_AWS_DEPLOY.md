# 🚀 Guia de Deploy AWS - Tech Challenge

## 🎯 **Visão Geral**

Este guia explica como fazer deploy da aplicação na AWS usando a arquitetura completa:
- **EKS** para aplicação Spring Boot
- **RDS PostgreSQL** para banco de dados  
- **Lambda + Cognito** para autenticação
- **API Gateway** para roteamento
- **GitHub Actions** para CI/CD automático

## 🏗️ **Arquitetura AWS**

```
┌─────────────────────────────────────────────────┐
│                    AWS CLOUD                    │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌─────────────┐    ┌─────────────────────────┐ │
│  │ API Gateway │───►│      Lambda Auth        │ │
│  │             │    │   (Cognito + JWT)       │ │
│  └─────────────┘    └─────────────────────────┘ │
│         │                                       │
│         ▼                                       │
│  ┌─────────────────────────────────────────────┐ │
│  │              EKS Cluster                    │ │
│  │  ┌─────────────────┐  ┌─────────────────┐   │ │
│  │  │  Spring Boot    │  │   PostgreSQL    │   │ │
│  │  │  Application    │──│   RDS Instance  │   │ │
│  │  └─────────────────┘  └─────────────────┘   │ │
│  └─────────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

## 📋 **Pré-requisitos**

### **1. Conta AWS**
- Conta AWS ativa
- Usuário IAM com permissões administrativas
- AWS CLI configurado

### **2. GitHub Secrets**
Configure nos 4 repositórios:

```bash
# AWS Credentials
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...

# Database
DB_HOST=tech-challenge-db.xxxxx.us-east-1.rds.amazonaws.com
DB_PASSWORD=MinhaSenh@123!

# Authentication  
COGNITO_USER_POOL_ID=us-east-1_...
COGNITO_CLIENT_ID=...
JWT_SECRET=meu-jwt-super-seguro
```

## 🚀 **Processo de Deploy**

### **Ordem de Execução:**

#### **1. Infraestrutura Core**
```bash
# Repositório: tech-challenge-infra-core
# Cria: VPC + EKS + Cognito + API Gateway

git push origin main
# GitHub Actions executa automaticamente:
# - terraform init
# - terraform plan  
# - terraform apply
```

#### **2. Banco de Dados**
```bash
# Repositório: tech-challenge-infra-database  
# Cria: RDS PostgreSQL

git push origin main
# GitHub Actions executa automaticamente:
# - terraform init
# - terraform plan -var="db_password=$DB_PASSWORD"
# - terraform apply
```

#### **3. Lambda de Autenticação**
```bash
# Repositório: tech-challenge-auth-lambda
# Deploy: Função Lambda + Environment Variables

git push origin main
# GitHub Actions executa automaticamente:
# - mvn package
# - aws lambda update-function-code
# - aws lambda update-function-configuration
```

#### **4. Aplicação Spring Boot**
```bash
# Repositório: tech-challenge-application
# Deploy: Docker → ECR → EKS

git push origin main
# GitHub Actions executa automaticamente:
# - mvn test
# - docker build
# - docker push ECR
# - kubectl apply
```

## 🔧 **Configuração Detalhada**

### **GitHub Actions - Application**

```yaml
# .github/workflows/main.yml
- name: Deploy to EKS
  run: |
    # Create namespace
    kubectl create namespace tech-challenge --dry-run=client -o yaml | kubectl apply -f -
    
    # Create ConfigMap
    kubectl create configmap app-config \
      --from-literal=DB_HOST=${{ secrets.DB_HOST }} \
      --from-literal=COGNITO_USER_POOL_ID=${{ secrets.COGNITO_USER_POOL_ID }} \
      --namespace=tech-challenge \
      --dry-run=client -o yaml | kubectl apply -f -
    
    # Create Secret
    kubectl create secret generic app-secrets \
      --from-literal=DB_PASSWORD="${{ secrets.DB_PASSWORD }}" \
      --from-literal=JWT_SECRET="${{ secrets.JWT_SECRET }}" \
      --namespace=tech-challenge \
      --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy application
    kubectl apply -f k8s/
```

### **Kubernetes Manifests**

```yaml
# k8s/deployment.yaml
env:
- name: DB_HOST
  valueFrom:
    configMapKeyRef:
      name: app-config
      key: DB_HOST
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: app-secrets
      key: DB_PASSWORD
- name: COGNITO_USER_POOL_ID
  valueFrom:
    configMapKeyRef:
      name: app-config
      key: COGNITO_USER_POOL_ID
```

## 🔍 **Verificação do Deploy**

### **1. Infraestrutura**
```bash
# Verificar recursos criados
aws eks describe-cluster --name tech-challenge-cluster
aws rds describe-db-instances --db-instance-identifier tech-challenge-db
aws cognito-idp describe-user-pool --user-pool-id us-east-1_...
```

### **2. Aplicação**
```bash
# Conectar ao EKS
aws eks update-kubeconfig --region us-east-1 --name tech-challenge-cluster

# Verificar pods
kubectl get pods -n tech-challenge

# Verificar logs
kubectl logs -l app=tech-challenge-app -n tech-challenge
```

### **3. Endpoints**
```bash
# Health check
curl https://api.tech-challenge.com/api/health

# Swagger UI
https://api.tech-challenge.com/api/swagger-ui/index.html

# Autenticação
curl -X POST https://api.tech-challenge.com/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678901"}'
```

## 🐛 **Troubleshooting**

### **Problema: EKS não conecta**
```bash
# Verificar kubeconfig
aws eks update-kubeconfig --region us-east-1 --name tech-challenge-cluster

# Verificar permissões
kubectl auth can-i "*" "*" --all-namespaces
```

### **Problema: RDS não conecta**
```bash
# Verificar security group
aws ec2 describe-security-groups --group-names tech-challenge-rds-sg

# Testar conectividade do pod
kubectl exec -it <pod-name> -n tech-challenge -- nc -zv $DB_HOST 5432
```

### **Problema: Lambda não funciona**
```bash
# Verificar logs
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/tech-challenge-auth

# Testar função
aws lambda invoke --function-name tech-challenge-auth-lambda \
  --payload '{"body":"{\"cpf\":\"12345678901\"}"}' response.json
```

### **Problema: Cognito não autentica**
```bash
# Verificar user pool
aws cognito-idp describe-user-pool --user-pool-id $COGNITO_USER_POOL_ID

# Listar usuários
aws cognito-idp list-users --user-pool-id $COGNITO_USER_POOL_ID
```

## 📊 **Monitoramento AWS**

### **CloudWatch Logs**
```bash
# Logs da aplicação
aws logs describe-log-groups --log-group-name-prefix /aws/eks/tech-challenge

# Logs do Lambda
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/tech-challenge-auth
```

### **CloudWatch Metrics**
- **EKS:** CPU, Memory, Network
- **RDS:** Connections, CPU, Storage
- **Lambda:** Duration, Errors, Invocations
- **API Gateway:** Requests, Latency, Errors

## 🔄 **Rollback**

### **Aplicação**
```bash
# Rollback deployment
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge

# Verificar histórico
kubectl rollout history deployment/tech-challenge-app -n tech-challenge
```

### **Infraestrutura**
```bash
# Revert commit no repositório
git revert <commit-hash>
git push origin main

# GitHub Actions executará terraform apply automaticamente
```

## 💰 **Custos Estimados**

| Recurso | Tipo | Custo Mensal (USD) |
|---------|------|-------------------|
| EKS Cluster | Control Plane | $72 |
| EC2 Instances | t3.medium (2x) | $60 |
| RDS PostgreSQL | db.t3.micro | $15 |
| Lambda | 1M requests | $0.20 |
| API Gateway | 1M requests | $3.50 |
| **Total** | | **~$150/mês** |

## 🎯 **Próximos Passos**

1. **Configurar domínio personalizado** no API Gateway
2. **Implementar HTTPS** com Certificate Manager
3. **Configurar backup automático** do RDS
4. **Implementar auto-scaling** no EKS
5. **Configurar alertas** no CloudWatch

**🚀 Deploy AWS configurado com sucesso!**