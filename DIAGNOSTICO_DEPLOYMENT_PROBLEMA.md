# 🚨 DIAGNÓSTICO: Deployment Application Falhando

## 📊 **ANÁLISE DOS LOGS CLOUDWATCH**

### **O que foi encontrado nos logs:**

✅ **Componentes do EKS funcionando:**
- kube-scheduler: Ativo
- kube-controller-manager: Ativo  
- aws-load-balancer-controller: Ativo (pod: `aws-load-balancer-controller-7f4fb958f6-8fmcw`)
- Node registrado: `ip-10-0-1-155.ec2.internal` (IP: 10.0.1.155)

❌ **Ausência CRÍTICA:**
- **ZERO logs do namespace `tech-challenge`**
- **ZERO eventos de pods da aplicação**
- **ZERO erros de scheduling ou image pull**

### **Conclusão da Análise:**
Os pods da aplicação **NÃO ESTÃO SENDO CRIADOS** no cluster.

---

## 🔍 **POSSÍVEIS CAUSAS DO PROBLEMA**

### **1. IMAGEM DOCKER NÃO EXISTE NO ECR** ⚠️ (Mais Provável)

O Terraform tenta criar o deployment com:
```terraform
image = "${var.ecr_repository_url}:latest"
```

**Problema**: Se a imagem `latest` não existe no ECR, o pod fica em estado `ImagePullBackOff` ou `ErrImagePull`.

**Verificação necessária:**
```bash
# Listar imagens no ECR
aws ecr describe-images \
  --repository-name tech-challenge-api \
  --region us-east-1

# Deve retornar pelo menos 1 imagem com tag "latest"
```

**Solução se imagem não existir:**
1. Build e push da imagem Docker para o ECR ANTES de rodar o Terraform da application
2. Ou mudar o Terraform para `wait_for_rollout = false` temporariamente

---

### **2. SECRETS/CONFIGMAPS INVÁLIDOS**

O deployment depende de:
- `data.terraform_remote_state.database.outputs.*` (RDS info)
- `data.terraform_remote_state.core.outputs.*` (EKS/Cognito info)

**Problema**: Se esses outputs não existem ou estão vazios, o ConfigMap pode ter valores inválidos.

**Verificação necessária:**
```bash
# Ver outputs do database
cd c:/Users/User/repositorios/tech-challenge-infra-database
terraform output

# Ver outputs do core
cd c:/Users/User/repositorios/tech-challenge-infra-core
terraform output
```

---

### **3. NODE SEM CAPACIDADE DE RECURSOS**

Node `t3.small` tem apenas:
- **2 GB RAM** 
- **2 vCPUs**

Recursos já consumidos:
- Sistema operacional: ~300-500MB
- Kube-system pods (aws-load-balancer-controller, coredns, etc): ~500-700MB
- **Disponível**: ~700MB-1GB

O deployment pede:
```terraform
requests = {
  memory = "512Mi"  # 512MB
  cpu    = "150m"   
}
```

**Problema**: Pode não ter espaço suficiente se outros pods estão rodando.

---

## ✅ **SOLUÇÕES RECOMENDADAS**

### **SOLUÇÃO 1: Build e Push da Imagem Docker PRIMEIRO** (PRIORITÁRIO)

A aplicação precisa ter uma imagem no ECR **ANTES** do Terraform criar o deployment.

```bash
cd c:/Users/User/repositorios/tech-challenge-application

# 1. Fazer login no ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# 2. Build da imagem
docker build -t tech-challenge-api:latest .

# 3. Tag para ECR
docker tag tech-challenge-api:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/tech-challenge-api:latest

# 4. Push para ECR
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/tech-challenge-api:latest
```

**OU** configure o GitHub Actions para fazer isso automaticamente antes do Terraform.

---

### **SOLUÇÃO 2: Modificar Terraform para não esperar rollout**

Adicione no `main.tf` do application:

```terraform
resource "kubernetes_deployment" "tech_challenge_app" {
  # ... configuração existente ...

  wait_for_rollout = false  # ADICIONAR ESTA LINHA

  # ... resto do código ...
}
```

Isso permite o Terraform completar sem esperar os pods ficarem ready.

---

### **SOLUÇÃO 3: Aumentar timeout do Terraform**

Crie/edite `terraform/timeouts.tf`:

```terraform
resource "kubernetes_deployment" "tech_challenge_app" {
  # ... configuração existente ...

  timeouts {
    create = "15m"  # Aumentar de 10m (padrão) para 15m
    update = "15m"
  }
}
```

---

## 🔧 **COMANDOS DE DIAGNÓSTICO NECESSÁRIOS**

Execute estes comandos para confirmar o problema:

```bash
# 1. Verificar se namespace existe
kubectl get namespace tech-challenge

# 2. Verificar deployments
kubectl get deployments -n tech-challenge

# 3. Verificar pods e seus estados
kubectl get pods -n tech-challenge -o wide

# 4. Ver eventos do namespace (CRÍTICO)
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'

# 5. Se pod existe, ver detalhes
kubectl describe pod -l app=tech-challenge-app -n tech-challenge

# 6. Ver logs do pod (se existir)
kubectl logs -l app=tech-challenge-app -n tech-challenge

# 7. Verificar nodes disponíveis
kubectl get nodes -o wide

# 8. Ver recursos disponíveis no node
kubectl describe node ip-10-0-1-155.ec2.internal
```

---

## 📋 **CHECKLIST DE CORREÇÃO**

- [ ] **1. Verificar se imagem existe no ECR**
  ```bash
  aws ecr describe-images --repository-name tech-challenge-api --region us-east-1
  ```

- [ ] **2. Se imagem NÃO existe: Build e Push**
  - Fazer build local OU
  - Rodar GitHub Actions do repositório application

- [ ] **3. Verificar outputs do Terraform database e core**
  ```bash
  cd tech-challenge-infra-database && terraform output
  cd tech-challenge-infra-core && terraform output
  ```

- [ ] **4. Ver eventos no namespace tech-challenge**
  ```bash
  kubectl get events -n tech-challenge
  ```

- [ ] **5. Se problema persiste: Adicionar `wait_for_rollout = false`**

- [ ] **6. Rerun do Terraform application**
  ```bash
  cd tech-challenge-application/terraform
  terraform init
  terraform plan
  terraform apply
  ```

---

## 🎯 **ORDEM DE EXECUÇÃO CORRETA**

Para evitar esse problema no futuro:

### **1. Infraestrutura Base**
```bash
cd tech-challenge-infra-core
terraform apply  # ✅ EKS, VPC, ECR, Cognito
```

### **2. Database**
```bash
cd tech-challenge-infra-database
terraform apply  # ✅ RDS PostgreSQL
```

### **3. Build e Push da Aplicação** ⚠️ **ESTE PASSO ESTAVA FALTANDO**
```bash
cd tech-challenge-application

# Build da imagem
docker build -t tech-challenge-api:latest .

# Push para ECR (obtém URL do core outputs)
ECR_URL=$(cd ../tech-challenge-infra-core && terraform output -raw ecr_repository_url)
docker tag tech-challenge-api:latest $ECR_URL:latest
docker push $ECR_URL:latest
```

### **4. Deploy da Aplicação no EKS**
```bash
cd tech-challenge-application/terraform
terraform apply  # ✅ Agora a imagem existe!
```

### **5. API Gateway**
```bash
cd tech-challenge-infra-gateway-lambda
terraform apply  # ✅ Exposição externa
```

---

## 📚 **LOGS PARA ANÁLISE ADICIONAL**

Se precisar de mais diagnósticos, colete:

1. **Logs do AWS Load Balancer Controller:**
   ```bash
   kubectl logs -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller
   ```

2. **Logs do CoreDNS:**
   ```bash
   kubectl logs -n kube-system -l k8s-app=kube-dns
   ```

3. **Eventos do cluster:**
   ```bash
   kubectl get events --all-namespaces --sort-by='.lastTimestamp' | tail -50
   ```

4. **Describe do node:**
   ```bash
   kubectl describe node ip-10-0-1-155.ec2.internal
   ```

---

## 🚀 **PRÓXIMO PASSO IMEDIATO**

**Execute este comando para confirmar se a imagem existe:**

```bash
aws ecr describe-images \
  --repository-name tech-challenge-api \
  --region us-east-1 \
  --query 'imageDetails[*].[imageTags[0],imagePushedAt]' \
  --output table
```

**Se retornar vazio** → Precisa fazer build e push da imagem PRIMEIRO
**Se retornar imagens** → O problema é outro (provavelmente resources/secrets)

---

## 💡 **SOLUÇÃO RÁPIDA TEMPORÁRIA**

Se você quiser que o Terraform complete SEM esperar os pods:

```bash
cd c:/Users/User/repositorios/tech-challenge-application/terraform

# Adicionar flag de não esperar
terraform apply -var="wait_for_rollout=false"
```

Mas isso apenas **adia o problema** - os pods ainda não vão funcionar sem a imagem no ECR.
