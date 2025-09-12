# 📱 Tutorial de Uso da API - Tech Challenge

## 🎯 **Visão Geral**

Este tutorial mostra como usar a API do Tech Challenge em produção na AWS, incluindo autenticação com Cognito e todos os endpoints disponíveis.

## 🔐 **Autenticação**

### **1. Registrar Cliente**
```bash
# Criar cliente (registra automaticamente no Cognito)
curl -X POST https://api.tech-challenge.com/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com", 
    "cpf": "12345678901"
  }'
```

### **2. Obter Token JWT**
```bash
# Autenticar com CPF via Lambda
curl -X POST https://api.tech-challenge.com/auth \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "12345678901"
  }'

# Resposta:
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "idToken": "eyJhbGciOiJSUzI1NiIs...", 
  "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
  "expiresIn": 3600
}
```

### **3. Usar Token nas Requisições**
```bash
# Salvar token em variável
export JWT_TOKEN="eyJhbGciOiJSUzI1NiIs..."

# Usar em requisições protegidas
curl -H "Authorization: Bearer $JWT_TOKEN" \
  https://api.tech-challenge.com/api/orders
```

## 🍔 **Fluxo Completo de Pedido**

### **Passo 1: Criar Categoria**
```bash
curl -X POST https://api.tech-challenge.com/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lanches"
  }'

# Resposta:
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Lanches"
}
```

### **Passo 2: Criar Produto**
```bash
curl -X POST https://api.tech-challenge.com/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hambúrguer Artesanal",
    "description": "Hambúrguer 180g com queijo, alface e tomate",
    "price": 25.90,
    "categoryId": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Resposta:
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "Hambúrguer Artesanal",
  "description": "Hambúrguer 180g com queijo, alface e tomate", 
  "price": 25.90,
  "categoryId": "550e8400-e29b-41d4-a716-446655440000",
  "active": true
}
```

### **Passo 3: Fazer Pedido (Autenticado)**
```bash
curl -X POST https://api.tech-challenge.com/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "customerId": "770e8400-e29b-41d4-a716-446655440002",
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440001",
        "quantity": 2
      }
    ]
  }'

# Resposta:
{
  "id": 1,
  "customerId": "770e8400-e29b-41d4-a716-446655440002",
  "customer": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "name": "João Silva",
    "email": "joao@email.com"
  },
  "items": [
    {
      "product": {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "name": "Hambúrguer Artesanal",
        "price": 25.90
      },
      "quantity": 2,
      "subTotal": 51.80
    }
  ],
  "totalAmount": 51.80,
  "status": "RECEIVED",
  "statusPayment": "AGUARDANDO_PAGAMENTO",
  "idPayment": 12345,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### **Passo 4: Simular Pagamento**
```bash
# Webhook do Mercado Pago (simulado)
curl -X POST https://api.tech-challenge.com/api/webhook/payment \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "id": "12345"
    }
  }'

# Resposta: 200 OK
```

### **Passo 5: Verificar Status**
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  https://api.tech-challenge.com/api/orders/1

# Resposta:
{
  "id": 1,
  "status": "IN_PREPARATION",
  "statusPayment": "APROVADO",
  "totalAmount": 51.80,
  ...
}
```

## 📋 **Endpoints Disponíveis**

### **🔓 Públicos (Sem Autenticação)**

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/health` | Status da aplicação |
| `GET` | `/api/categories` | Listar categorias |
| `POST` | `/api/categories` | Criar categoria |
| `GET` | `/api/products` | Listar produtos |
| `POST` | `/api/products` | Criar produto |
| `POST` | `/api/customers` | Registrar cliente |
| `POST` | `/api/webhook/payment` | Webhook pagamento |

### **🔒 Protegidos (Requer JWT)**

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/customers` | Listar clientes |
| `GET` | `/api/orders` | Listar pedidos |
| `POST` | `/api/orders` | Criar pedido |
| `GET` | `/api/orders/{id}` | Buscar pedido |
| `PUT` | `/api/orders/{id}/status` | Atualizar status |

## 🧪 **Cenários de Teste**

### **Pedido Anônimo**
```bash
# Pedido sem cliente (não requer autenticação)
curl -X POST https://api.tech-challenge.com/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": null,
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440001",
        "quantity": 1
      }
    ]
  }'
```

### **Múltiplos Produtos**
```bash
curl -X POST https://api.tech-challenge.com/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "customerId": "770e8400-e29b-41d4-a716-446655440002",
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440001",
        "quantity": 1
      },
      {
        "productId": "770e8400-e29b-41d4-a716-446655440003", 
        "quantity": 2
      }
    ]
  }'
```

### **Validações de Erro**
```bash
# Produto inexistente
curl -X POST https://api.tech-challenge.com/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": null,
    "items": [
      {
        "productId": "00000000-0000-0000-0000-000000000000",
        "quantity": 1
      }
    ]
  }'
# Retorna: 404 Not Found - "Product not found"

# Quantidade inválida  
curl -X POST https://api.tech-challenge.com/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": null,
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440001",
        "quantity": 0
      }
    ]
  }'
# Retorna: 400 Bad Request - "Quantity must be greater than zero"
```

## 👨‍🍳 **Operações da Cozinha**

### **Listar Pedidos em Preparo**
```bash
curl "https://api.tech-challenge.com/api/orders?status=IN_PREPARATION"
```

### **Marcar Pedido como Pronto**
```bash
curl -X PUT https://api.tech-challenge.com/api/orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "status": "READY"
  }'
```

### **Finalizar Pedido**
```bash
curl -X PUT https://api.tech-challenge.com/api/orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "status": "FINISHED"
  }'
```

## 📊 **Swagger UI**

Acesse a documentação interativa:
**https://api.tech-challenge.com/api/swagger-ui/index.html**

### **Recursos do Swagger:**
- ✅ Testar endpoints diretamente
- ✅ Ver schemas de request/response
- ✅ Autenticação JWT integrada
- ✅ Exemplos de uso

## 🔍 **Monitoramento**

### **Health Check**
```bash
curl https://api.tech-challenge.com/api/health

# Resposta:
{
  "status": "UP",
  "service": "tech-challenge",
  "timestamp": 1642234567890
}
```

### **Métricas**
```bash
curl https://api.tech-challenge.com/api/actuator/prometheus
# Retorna métricas em formato Prometheus
```

## 🐛 **Troubleshooting**

### **Token Expirado**
```bash
# Erro 401 Unauthorized
# Solução: Obter novo token
curl -X POST https://api.tech-challenge.com/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678901"}'
```

### **Cliente Não Encontrado**
```bash
# Erro: "Cliente não encontrado. Realize o cadastro primeiro."
# Solução: Registrar cliente primeiro
curl -X POST https://api.tech-challenge.com/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@email.com","cpf":"12345678901"}'
```

### **Pedido Não Pago**
```bash
# Erro: "The order is not paid"
# Solução: Simular pagamento
curl -X POST https://api.tech-challenge.com/api/webhook/payment \
  -H "Content-Type: application/json" \
  -d '{"data":{"id":"ID_DO_PAGAMENTO"}}'
```

## 📱 **Collection Postman**

Importe a collection para testes:
`postman/Tech_Challenge_API.postman_collection.json`

**Configuração:**
1. Importe a collection
2. Configure variável `base_url`: `https://api.tech-challenge.com`
3. Configure variável `jwt_token` após autenticação
4. Execute os requests em sequência

**🎉 API pronta para uso em produção!**