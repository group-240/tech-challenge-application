# 📊 Guia Completo de Observabilidade - Tech Challenge

## 🎯 Visão Geral

Este guia explica **onde encontrar logs**, **como monitorar a aplicação** e **as melhores práticas de logging** implementadas no Tech Challenge.

---

## 📍 Locais dos Logs - AWS EKS

### 1. **Logs dos Pods (Kubernetes)**

#### Via `kubectl` (Linha de Comando)

```bash
# 1. Configure o acesso ao cluster EKS
aws eks update-kubeconfig --region us-east-1 --name tech-challenge-eks

# 2. Liste os pods da aplicação
kubectl get pods -n tech-challenge

# 3. Veja os logs em tempo real de um pod específico
kubectl logs -f <pod-name> -n tech-challenge

# Exemplo:
kubectl logs -f tech-challenge-app-7fccc7678-ss5c7 -n tech-challenge

# 4. Veja os logs das últimas 100 linhas
kubectl logs --tail=100 <pod-name> -n tech-challenge

# 5. Veja logs de um container específico (se tiver múltiplos)
kubectl logs <pod-name> -c tech-challenge-app -n tech-challenge

# 6. Veja logs de um pod anterior (se crashou e foi recriado)
kubectl logs --previous <pod-name> -n tech-challenge

# 7. Veja logs com timestamp
kubectl logs <pod-name> -n tech-challenge --timestamps=true

# 8. Filtre logs por texto (usando grep)
kubectl logs <pod-name> -n tech-challenge | grep ERROR
kubectl logs <pod-name> -n tech-challenge | grep -i "exception"
```

#### Via AWS Console (CloudWatch Logs)

Os logs dos pods do EKS são **automaticamente enviados para CloudWatch Logs** se configurado.

**Localização:**
1. AWS Console → **CloudWatch**
2. **Logs** → **Log groups**
3. Procure por:
   - `/aws/eks/tech-challenge-eks/cluster` (logs do control plane)
   - `/aws/containerinsights/tech-challenge-eks/application` (logs dos containers)

**📌 IMPORTANTE:** Por padrão, os logs dos pods NÃO vão automaticamente para CloudWatch. É necessário instalar o **Fluent Bit** ou **CloudWatch Container Insights**.

---

### 2. **Logs do Cluster EKS (Control Plane)**

Estes logs são do **Kubernetes Control Plane** (API Server, Scheduler, etc.).

**Localização:**
- AWS Console → **CloudWatch** → **Log groups**
- Log group: `/aws/eks/tech-challenge-eks/cluster`

**Tipos de logs habilitados:**
- `api` - Chamadas à API do Kubernetes
- `audit` - Auditoria de ações
- `authenticator` - Autenticação
- `controllerManager` - Controller Manager
- `scheduler` - Scheduler

**Retenção:** 3 dias (configurado no Terraform)

---

### 3. **Logs de Deploy (GitHub Actions)**

**Localização:**
- GitHub → **Actions** → Workflow específico
- URL: https://github.com/group-240/tech-challenge-application/actions

**O que ver:**
- Build do Maven
- Testes executados
- Build da imagem Docker
- Push para ECR
- Execução do Terraform
- Status do deploy no EKS

---

## 🔍 Como Monitorar Deploy e Saúde da Aplicação

### **1. Durante o Deploy**

```bash
# Acompanhe o status do deploy
kubectl rollout status deployment/tech-challenge-app -n tech-challenge

# Veja os eventos em tempo real
kubectl get events -n tech-challenge --sort-by='.lastTimestamp' --watch

# Veja os pods e seu status
kubectl get pods -n tech-challenge -w
```

### **2. Verificar Saúde da Aplicação**

```bash
# Veja detalhes do pod (incluindo health checks)
kubectl describe pod <pod-name> -n tech-challenge

# Veja status do deployment
kubectl get deployment tech-challenge-app -n tech-challenge

# Veja se os endpoints estão saudáveis
kubectl get endpoints -n tech-challenge

# Teste o health check internamente
kubectl exec -it <pod-name> -n tech-challenge -- curl http://localhost:8080/api/health
```

### **3. Métricas da Aplicação**

```bash
# Veja métricas do Actuator (dentro do pod)
kubectl exec -it <pod-name> -n tech-challenge -- curl http://localhost:8080/api/metrics

# Veja uso de recursos dos pods
kubectl top pods -n tech-challenge

# Veja uso de recursos dos nodes
kubectl top nodes
```

---

## 📝 Formato de Logs - JSON Estruturado

### **✅ Melhores Práticas Implementadas**

A aplicação está configurada para gerar logs em **formato JSON** no ambiente de **dev/prod**, facilitando:
- ✅ Parsing automático por ferramentas de observabilidade
- ✅ Busca e filtragem eficiente
- ✅ Integração com CloudWatch Insights
- ✅ Análise com ferramentas de APM

### **Exemplo de Log JSON**

```json
{
  "timestamp": "2025-10-06T14:30:45.123Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.fiap.techchallenge.application.usecases.order.CreateOrderUseCase",
  "message": "Order created successfully",
  "application": "tech-challenge-api",
  "environment": "dev",
  "orderId": "ORD-123456",
  "customerId": "CUST-789",
  "totalAmount": 45.90
}
```

### **Exemplo de Log de Erro JSON**

```json
{
  "timestamp": "2025-10-06T14:32:10.456Z",
  "level": "ERROR",
  "thread": "http-nio-8080-exec-2",
  "logger": "com.fiap.techchallenge.external.datasource.repositories.OrderRepositoryImpl",
  "message": "Failed to save order to database",
  "application": "tech-challenge-api",
  "environment": "dev",
  "exception_class": "org.springframework.dao.DataIntegrityViolationException",
  "stack_trace": "org.springframework.dao.DataIntegrityViolationException: could not execute statement\n\tat org.hibernate.exception.internal.SQLExceptionTypeDelegate.convert(...)"
}
```

### **Campos Incluídos nos Logs JSON**

| Campo | Descrição |
|-------|-----------|
| `timestamp` | Data/hora em ISO 8601 |
| `level` | Nível do log (INFO, WARN, ERROR, DEBUG) |
| `thread` | Thread que gerou o log |
| `logger` | Classe que gerou o log |
| `message` | Mensagem do log |
| `application` | Nome da aplicação |
| `environment` | Ambiente (dev, prod) |
| `stack_trace` | Stack trace completo (apenas em erros) |
| Campos customizados | Dados adicionais do contexto |

---

## 🛠️ Configuração de Logs

### **Profiles de Log**

#### **1. Local Development** (`local` ou `default`)
- **Formato:** Texto legível (human-readable)
- **Nível:** DEBUG para aplicação
- **Console:** Colorido e formatado

```bash
# Executar localmente
./mvnw spring-boot:run
```

#### **2. Dev/Prod** (`dev` ou `prod`)
- **Formato:** JSON estruturado
- **Nível:** INFO geral, DEBUG para aplicação
- **Console:** JSON para parsing

```bash
# Executar com profile dev
java -jar -Dspring.profiles.active=dev tech-challenge.jar
```

### **Arquivos de Configuração**

| Arquivo | Propósito |
|---------|-----------|
| `logback-spring.xml` | Configuração principal de logs (JSON/Console) |
| `application.yml` | Níveis de log e configurações do Actuator |
| `pom.xml` | Dependência `logstash-logback-encoder` |

---

## 📊 CloudWatch Logs Insights - Queries Úteis

Se os logs estiverem no CloudWatch, você pode usar **CloudWatch Logs Insights** para queries:

### **1. Buscar Erros nas Últimas 24h**

```
fields @timestamp, level, logger, message, exception_class
| filter level = "ERROR"
| sort @timestamp desc
| limit 100
```

### **2. Buscar Logs de um Endpoint Específico**

```
fields @timestamp, message, logger
| filter message like /order/
| sort @timestamp desc
| limit 50
```

### **3. Contar Erros por Tipo**

```
fields exception_class
| filter level = "ERROR"
| stats count() by exception_class
```

### **4. Tempo de Resposta Médio**

```
fields @timestamp, message, responseTime
| filter message like /completed/
| stats avg(responseTime) as avgResponseTime
```

---

## 🚨 Troubleshooting - Problemas Comuns

### **Problema 1: Pods não iniciam**

```bash
# Veja os eventos
kubectl describe pod <pod-name> -n tech-challenge

# Veja os logs
kubectl logs <pod-name> -n tech-challenge

# Causas comuns:
# - Imagem não encontrada no ECR
# - Falta de recursos (memória/CPU)
# - Health checks falhando
# - Variáveis de ambiente faltando
```

### **Problema 2: Health Checks falhando**

```bash
# Teste o endpoint de health manualmente
kubectl exec -it <pod-name> -n tech-challenge -- curl -v http://localhost:8080/api/health

# Veja os logs da aplicação
kubectl logs <pod-name> -n tech-challenge | grep -i health

# Verifique se a aplicação iniciou
kubectl logs <pod-name> -n tech-challenge | grep "Started TechChallengeApplication"
```

### **Problema 3: Aplicação crashando**

```bash
# Veja logs do pod anterior
kubectl logs --previous <pod-name> -n tech-challenge

# Veja eventos do pod
kubectl describe pod <pod-name> -n tech-challenge

# Veja se há erros de memória
kubectl top pod <pod-name> -n tech-challenge
```

### **Problema 4: Não consigo ver logs**

```bash
# Verifique se o pod está rodando
kubectl get pods -n tech-challenge

# Verifique se o namespace existe
kubectl get namespaces

# Configure novamente o kubeconfig
aws eks update-kubeconfig --region us-east-1 --name tech-challenge-eks
```

---

## 📈 Monitoramento Contínuo

### **1. Dashboard de Pods**

```bash
# Abra o Kubernetes Dashboard (se instalado)
kubectl proxy

# Ou use k9s (CLI interativa)
k9s -n tech-challenge
```

### **2. Alertas e Notificações**

Configure alertas no CloudWatch para:
- ✅ Erros acima de threshold
- ✅ Pods crashando
- ✅ Memória/CPU alta
- ✅ Health checks falhando

---

## 🎓 Comandos Essenciais - Cheat Sheet

```bash
# Ver logs em tempo real
kubectl logs -f <pod-name> -n tech-challenge

# Ver logs das últimas 1h
kubectl logs --since=1h <pod-name> -n tech-challenge

# Ver logs com grep
kubectl logs <pod-name> -n tech-challenge | grep -i error

# Ver eventos
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'

# Entrar no pod (debug)
kubectl exec -it <pod-name> -n tech-challenge -- /bin/bash

# Ver configurações do pod
kubectl get pod <pod-name> -n tech-challenge -o yaml

# Ver logs do deployment
kubectl describe deployment tech-challenge-app -n tech-challenge

# Ver status de todos os recursos
kubectl get all -n tech-challenge
```

---

## 🔗 Links Úteis

| Recurso | Link |
|---------|------|
| GitHub Actions | https://github.com/group-240/tech-challenge-application/actions |
| AWS Console - EKS | https://console.aws.amazon.com/eks |
| AWS Console - CloudWatch | https://console.aws.amazon.com/cloudwatch |
| Kubernetes Docs - Logs | https://kubernetes.io/docs/concepts/cluster-administration/logging/ |
| Logback JSON Encoder | https://github.com/logfellow/logstash-logback-encoder |

---

## ✅ Checklist de Observabilidade

- [x] Logs estruturados em JSON (produção)
- [x] Logs legíveis para desenvolvimento
- [x] Health checks configurados
- [x] Actuator com métricas
- [x] Campos contextuais nos logs
- [x] Stack traces completos em erros
- [x] Timestamp em ISO 8601
- [x] Application e environment nos logs
- [x] Logs de SQL queries (quando necessário)
- [x] Retenção de logs configurada

---

**🎯 Próximo Passo:** Configure o **CloudWatch Container Insights** para enviar logs dos pods automaticamente para CloudWatch!
