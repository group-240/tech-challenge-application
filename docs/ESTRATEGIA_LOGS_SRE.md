# 📊 Estratégia SRE de Logging - Tech Challenge

## 🎯 Visão Geral

Esta aplicação implementa **práticas de logging do Google SRE** (Site Reliability Engineering) com **logs estruturados em JSON** para o ambiente **dev**.

Baseado em: [Google SRE Book - Monitoring Distributed Systems](https://sre.google/sre-book/monitoring-distributed-systems/)

---

## 📋 Categorização de Logs SRE

### **Níveis de Severidade**

| Nível | Quando Usar | Exemplos | Ação Requerida |
|-------|-------------|----------|----------------|
| **DEBUG** | Diagnóstico detalhado em desenvolvimento | Valores de variáveis, fluxo de execução | Nenhuma (dev only) |
| **INFO** | Eventos normais e esperados | Order created, Payment processed | Nenhuma |
| **WARN** | Situações anormais MAS recuperáveis | Retry bem-sucedido, Cache miss, Recurso próximo do limite | Investigar se recorrente |
| **ERROR** | Falhas que impedem uma operação | Exception, Timeout, Falha de integração | Investigar ASAP |
| **FATAL** | Falhas críticas do sistema | OOM, Falha no startup, Perda de BD | **ALERTA IMEDIATO** |

---

## 🔍 Categorização Detalhada

### **1. INFO - Eventos Normais** ✅

**Quando usar:**
- ✅ Operações bem-sucedidas
- ✅ Marcos importantes do fluxo
- ✅ Inicialização de componentes
- ✅ Conclusão de processamentos

**Exemplos corretos:**
```java
// ✅ BOM: Operação de negócio bem-sucedida
logger.info("Order created successfully: orderId={}, customerId={}, amount={}", 
            orderId, customerId, amount);

// ✅ BOM: Integração externa bem-sucedida
logger.info("Payment processed via Mercado Pago: transactionId={}, amount={}", 
            transactionId, amount);

// ✅ BOM: Inicialização
logger.info("Application started successfully on port {}", port);
```

**❌ Exemplos ERRADOS:**
```java
// ❌ RUIM: Muito verboso (use DEBUG)
logger.info("Entering method createOrder with parameters: {}", params);

// ❌ RUIM: Informação inútil
logger.info("Loop iteration {}", i);

// ❌ RUIM: Deve ser WARN (situação anormal)
logger.info("Product not found in cache, fetching from database");
```

---

### **2. WARN - Situações Anormais (Mas Recuperáveis)** ⚠️

**Quando usar:**
- ⚠️ Sistema se recuperou, mas algo não estava ideal
- ⚠️ Configuração faltando (usando default)
- ⚠️ Performance degradada
- ⚠️ Validação de negócio esperada
- ⚠️ Cache miss ou fallback

**Exemplos corretos:**
```java
// ✅ BOM: Retry bem-sucedido após falha
logger.warn("External API call failed, retry {} of {} succeeded", retryCount, maxRetries);

// ✅ BOM: Performance degradada
logger.warn("Database query took {}ms, exceeding threshold of {}ms", duration, threshold);

// ✅ BOM: Cache miss (degradação de performance)
logger.warn("Product {} not found in cache, fetching from database: productId={}", 
            productName, productId);

// ✅ BOM: Validação de negócio esperada
logger.warn("Customer CPF already exists, returning existing customer: cpf={}", cpf);

// ✅ BOM: Recurso próximo do limite
logger.warn("Database connection pool at {}% capacity", utilizationPercent);

// ✅ BOM: Configuração faltando
logger.warn("API_TIMEOUT not configured, using default value: {}ms", defaultTimeout);
```

**❌ Exemplos ERRADOS:**
```java
// ❌ RUIM: Deve ser INFO (operação bem-sucedida)
logger.warn("Order created successfully");

// ❌ RUIM: Deve ser ERROR (falha não recuperada)
logger.warn("Failed to connect to database after 3 retries");

// ❌ RUIM: Muito verboso
logger.warn("Method took 100ms to execute");
```

**⚠️ IMPORTANTE:** WARN NÃO é erro! Sistema está funcionando, mas não de forma ideal.

---

### **3. ERROR - Falhas de Operação** 🔴

**Quando usar:**
- 🔴 Exceções inesperadas
- 🔴 Falha de integração com API externa
- 🔴 Erro de banco de dados
- 🔴 Timeout
- 🔴 Falha de autenticação

**Exemplos corretos:**
```java
// ✅ BOM: Exceção com stack trace
try {
    orderRepository.save(order);
} catch (DataAccessException e) {
    StructuredLogger.setError("DATABASE_SAVE_FAILED", e.getMessage());
    logger.error("Failed to save order to database: orderId={}", orderId, e);
    throw e;
}

// ✅ BOM: Timeout de API externa
logger.error("Payment gateway timeout after {}s: transactionId={}", 
            timeoutSeconds, transactionId);

// ✅ BOM: Falha de autenticação
logger.error("Failed to authenticate with Cognito: userId={}, error={}", 
            userId, errorMessage);
```

**❌ Exemplos ERRADOS:**
```java
// ❌ RUIM: Deve ser WARN (recuperável)
logger.error("Retry attempt 2 of 3");

// ❌ RUIM: Sem stack trace (sempre incluir exceção)
try {
    // código
} catch (Exception e) {
    logger.error("Error: " + e.getMessage()); // ❌ Sem stack trace!
}

// ❌ RUIM: Deve ser INFO (validação esperada)
logger.error("Customer not found with CPF: {}", cpf);
```

**⚠️ SEMPRE incluir a exceção completa:** `logger.error("Message", exception)`

---

### **4. FATAL - Falhas Críticas** 💀

**Quando usar:**
- 💀 Falha ao iniciar aplicação
- 💀 OutOfMemoryError
- 💀 Perda de conexão com recurso crítico
- 💀 Corrupção de dados

**Exemplos corretos:**
```java
// ✅ BOM: Falha crítica no startup
try {
    dataSource.getConnection();
} catch (SQLException e) {
    logger.error("FATAL: Failed to connect to database on startup", e);
    System.exit(1); // Aplicação não pode continuar
}

// ✅ BOM: OutOfMemoryError
catch (OutOfMemoryError e) {
    logger.error("FATAL: OutOfMemoryError - Application terminating", e);
    System.exit(1);
}
```

**⚠️ Sistema está INDISPONÍVEL após FATAL!**

---

## 🏷️ Categorias Especiais SRE

Além da severidade, usamos **categorias** para facilitar análise:

### **BUSINESS** - Eventos de Negócio

```java
StructuredLogger.setCategory(LogCategory.BUSINESS);
StructuredLogger.setOperation("CreateOrder");
StructuredLogger.setOrderId(orderId);

logger.info("Order created successfully");
```

**Quando usar:**
- Criação de pedidos
- Processamento de pagamentos
- Registro de clientes
- Mudanças de status importantes

### **SECURITY** - Eventos de Segurança

```java
StructuredLogger.setCategory(LogCategory.SECURITY);
StructuredLogger.setUserId(userId);
StructuredLogger.put("action", "ACCESS_DENIED");

logger.warn("Unauthorized access attempt detected");
```

**Quando usar:**
- Tentativas de acesso não autorizado
- Falhas de autenticação
- Violações de rate limit
- Detecção de atividades suspeitas

### **AUDIT** - Auditoria

```java
StructuredLogger.setCategory(LogCategory.AUDIT);
StructuredLogger.setUserId(userId);
StructuredLogger.put("action", "DELETE_PRODUCT");
StructuredLogger.put("resource", productId);

logger.info("Product deleted by admin");
```

**Quando usar:**
- Ações administrativas
- Alterações em dados sensíveis
- Rastreabilidade legal

### **PERFORMANCE** - Métricas de Performance

```java
StructuredLogger.setCategory(LogCategory.PERFORMANCE);
StructuredLogger.setDuration(executionTime);

if (executionTime > 1000) {
    logger.warn("Slow database query detected: {}ms", executionTime);
}
```

**Quando usar:**
- Queries lentas
- Latência de APIs
- Uso de recursos

### **INTEGRATION** - Integrações Externas

```java
StructuredLogger.setCategory(LogCategory.INTEGRATION);
StructuredLogger.setOperation("CallMercadoPago");
StructuredLogger.setDuration(duration);

logger.info("Payment gateway response received");
```

**Quando usar:**
- Chamadas para APIs externas
- Webhooks recebidos
- Mensageria (SQS, SNS)

---

## 📊 Formato JSON dos Logs

Todos os logs são gerados em **JSON estruturado**:

```json
{
  "timestamp": "2025-10-06T14:30:45.123Z",
  "severity": "INFO",
  "logger_name": "com.fiap.techchallenge.application.usecases.order.CreateOrderUseCase",
  "thread": "http-nio-8080-exec-1",
  "message": "Order created successfully",
  "application": "tech-challenge-api",
  "environment": "dev",
  "service": "tech-challenge-api",
  "correlationId": "abc-123-def-456",
  "log_category": "BUSINESS",
  "operation": "CreateOrder",
  "orderId": "ORD-789",
  "customerId": "CUST-456",
  "amount": "150.00",
  "duration_ms": "245"
}
```

### **Campos Padrão**

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `timestamp` | string | Data/hora em UTC (ISO 8601) |
| `severity` | string | Nível do log (INFO, WARN, ERROR, FATAL) |
| `logger_name` | string | Classe que gerou o log |
| `thread` | string | Thread da execução |
| `message` | string | Mensagem descritiva |
| `application` | string | Nome da aplicação |
| `environment` | string | Ambiente (sempre "dev") |
| `service` | string | Nome do serviço |

### **Campos Contextuais**

| Campo | Tipo | Descrição | Quando Presente |
|-------|------|-----------|-----------------|
| `correlationId` | string | ID para rastreamento end-to-end | Todas requisições HTTP |
| `log_category` | string | Categoria (BUSINESS, SECURITY, etc.) | Quando definido |
| `operation` | string | Operação sendo executada | Quando definido |
| `userId` | string | ID do usuário | Quando disponível |
| `orderId` | string | ID do pedido | Operações de pedido |
| `customerId` | string | ID do cliente | Operações de cliente |
| `paymentId` | string | ID do pagamento | Operações de pagamento |
| `productId` | string | ID do produto | Operações de produto |
| `duration_ms` | number | Duração em milissegundos | Operações medidas |
| `http_method` | string | Método HTTP | Requisições HTTP |
| `http_status` | number | Status code HTTP | Respostas HTTP |
| `endpoint` | string | Endpoint da requisição | Requisições HTTP |
| `error_code` | string | Código do erro | Quando há erro |
| `error_message` | string | Mensagem do erro | Quando há erro |
| `stack_trace` | string | Stack trace completo | Apenas em ERROR/FATAL |

---

## 💻 Como Usar na Aplicação

### **1. Importar as classes**

```java
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

### **2. Criar logger na classe**

```java
private static final Logger logger = LoggerFactory.getLogger(MinhaClasse.class);
```

### **3. Usar StructuredLogger para contexto**

```java
public void createOrder(CreateOrderDTO dto) {
    long startTime = System.currentTimeMillis();
    
    try {
        // Adicionar contexto SRE
        StructuredLogger.generateCorrelationId(); // Gera automaticamente
        StructuredLogger.setCategory(LogCategory.BUSINESS);
        StructuredLogger.setOperation("CreateOrder");
        StructuredLogger.setCustomerId(dto.getCustomerId());
        StructuredLogger.put("amount", String.valueOf(dto.getTotalAmount()));
        
        // INFO: Operação iniciada
        logger.info("Order creation started");
        
        // Lógica de negócio...
        Order order = orderService.create(dto);
        
        StructuredLogger.setOrderId(order.getId());
        
        // INFO: Operação concluída
        long duration = System.currentTimeMillis() - startTime;
        StructuredLogger.setDuration(duration);
        logger.info("Order created successfully");
        
        return order;
        
    } catch (BusinessException e) {
        // WARN: Validação de negócio (esperado)
        logger.warn("Order validation failed: {}", e.getMessage());
        throw e;
        
    } catch (Exception e) {
        // ERROR: Erro inesperado
        StructuredLogger.setError("ORDER_CREATION_FAILED", e.getMessage());
        logger.error("Failed to create order", e); // ⚠️ Sempre incluir exceção!
        throw e;
        
    } finally {
        // SEMPRE limpar contexto
        StructuredLogger.clear();
    }
}
```

---

## 🔍 Queries Úteis

### **CloudWatch Logs Insights**

```sql
-- Buscar todos os ERRORs
fields @timestamp, severity, message, error_code, error_message, stack_trace
| filter severity = "ERROR"
| sort @timestamp desc
| limit 100

-- Buscar operações lentas (>500ms)
fields @timestamp, operation, duration_ms, message
| filter duration_ms > 500
| sort duration_ms desc
| limit 50

-- Buscar erros por tipo
fields error_code, error_message
| filter severity = "ERROR"
| stats count() by error_code
| sort count desc

-- Buscar eventos de segurança
fields @timestamp, userId, message, endpoint
| filter log_category = "SECURITY"
| sort @timestamp desc

-- Buscar eventos de negócio
fields @timestamp, operation, orderId, customerId, amount
| filter log_category = "BUSINESS"
| sort @timestamp desc
```

### **kubectl**

```bash
# Buscar ERRORs
kubectl logs deployment/tech-challenge-app -n tech-challenge | grep '"severity":"ERROR"'

# Buscar por correlationId
kubectl logs deployment/tech-challenge-app -n tech-challenge | grep '"correlationId":"abc-123"'

# Buscar eventos de negócio
kubectl logs deployment/tech-challenge-app -n tech-challenge | grep '"log_category":"BUSINESS"'
```

---

## ✅ Checklist SRE

Ao adicionar logs, pergunte:

- [ ] **Severidade correta?** (INFO/WARN/ERROR/FATAL)
- [ ] **Mensagem clara?** Descreve o que aconteceu?
- [ ] **Contexto suficiente?** Tem IDs relevantes?
- [ ] **Categoria definida?** (BUSINESS, SECURITY, etc.)
- [ ] **Exceção incluída?** (se for ERROR)
- [ ] **MDC limpo?** (`StructuredLogger.clear()` no finally)
- [ ] **Formato JSON?** Todos os logs estão estruturados?

---

## 📚 Referências

- [Google SRE Book - Monitoring](https://sre.google/sre-book/monitoring-distributed-systems/)
- [Logback JSON Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [SLF4J Documentation](http://www.slf4j.org/)
- [MDC (Mapped Diagnostic Context)](http://logback.qos.ch/manual/mdc.html)

---

## 🎯 Resumo

✅ **Ambiente:** dev (somente JSON)  
✅ **Formato:** JSON estruturado  
✅ **Severidade:** INFO, WARN, ERROR, FATAL  
✅ **Categorias:** BUSINESS, SECURITY, AUDIT, PERFORMANCE, INTEGRATION  
✅ **Contexto:** MDC com correlation ID e campos relevantes  
✅ **Rastreabilidade:** Correlation ID em todas as requisições  
✅ **Performance:** Duração de operações medida automaticamente  

**Logs profissionais seguindo práticas do Google SRE! 🚀**
