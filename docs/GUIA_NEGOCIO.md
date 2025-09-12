# 🚀 Guia de Regras de Negócio - Sistema de Pedidos

> **🎯 Objetivo:** Entender como funciona o sistema de pedidos de comida, suas regras de negócio e fluxos de dados.

---

## 🍔 Visão Geral do Negócio

Este sistema simula uma **lanchonete digital** onde:
- 👥 **Clientes** fazem pedidos (opcionalmente se identificam)
- 🍕 **Produtos** são organizados em categorias
- 📋 **Pedidos** passam por diferentes estados
- 💳 **Pagamentos** são processados via Mercado Pago
- 👨‍🍳 **Cozinha** acompanha o preparo dos pedidos

### 🎭 **Personas do Sistema**
- **🛒 Cliente:** Pessoa que faz pedidos
- **👨‍🍳 Cozinheiro:** Prepara os pedidos
- **💰 Caixa:** Recebe pagamentos
- **👨‍💼 Gerente:** Administra produtos e categorias

---

## 📊 Modelo de Domínio

### 🏢 **Entidades Principais**

```
📁 CATEGORIA
├── 🆔 ID (UUID)
└── 📝 Nome

📦 PRODUTO  
├── 🆔 ID (UUID)
├── 📝 Nome
├── 📄 Descrição
├── 💰 Preço
├── 📁 Categoria
└── ✅ Ativo

👤 CLIENTE
├── 🆔 ID (UUID)
├── 👤 Nome
├── 📧 Email
└── 📱 CPF

📋 PEDIDO
├── 🆔 ID (Long)
├── 👤 Cliente (opcional)
├── 📦 Itens do Pedido
├── 💰 Valor Total
├── 📊 Status do Pedido
├── 💳 Status do Pagamento
├── 🆔 ID do Pagamento
├── 📅 Data de Criação
└── 📅 Data de Atualização

📦 ITEM DO PEDIDO
├── 📦 Produto
├── 🔢 Quantidade
└── 💰 Subtotal
```

---

## 🔄 Estados e Transições

### 📊 **Status do Pedido (OrderStatus)**

```
🆕 RECEIVED (Recebido)
    ↓
    ✅ Pagamento aprovado
    ↓
🍳 IN_PREPARATION (Em Preparo)
    ↓
    👨‍🍳 Cozinha finaliza
    ↓
✅ READY (Pronto)
    ↓
    🛒 Cliente retira
    ↓
🏁 FINISHED (Finalizado)
```

### 💳 **Status do Pagamento (StatusPayment)**

```
⏳ AGUARDANDO_PAGAMENTO
    ↓
    💳 Webhook do Mercado Pago
    ↓
✅ APROVADO  ou  ❌ REJEITADO
```

### 🔄 **Regras de Transição**

#### 📋 **Pedido**
```java
// ✅ Regra: Só pode mudar status se pagamento aprovado
@Override
public Order updateOrderStatus(Long id, OrderStatus status) {
    Order existingOrder = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Record not found"));

    // 🚫 REGRA DE NEGÓCIO: Pedido deve estar pago
    if (!existingOrder.getStatusPayment().equals(StatusPayment.APROVADO)) {
        throw new DomainException("The order is not paid");
    }

    existingOrder.setStatus(status);
    existingOrder.setUpdatedAt(LocalDateTime.now());
    return orderRepository.save(existingOrder);
}
```

#### 💳 **Pagamento**
```java
// ✅ Regra: Pagamento aprovado automaticamente inicia preparo
@Override
public Order updateOrderStatusPayment(Long id, StatusPayment statusPayment) {
    Order existingOrder = orderRepository.findByIdPayment(id)
            .orElseThrow(() -> new NotFoundException("Record not found"));

    existingOrder.setStatusPayment(statusPayment);
    
    // 🚫 REGRA DE NEGÓCIO: Pagamento aprovado = preparo automático
    if (statusPayment == StatusPayment.APROVADO) {
        existingOrder.setStatus(OrderStatus.IN_PREPARATION);
    }
    
    existingOrder.setUpdatedAt(LocalDateTime.now());
    return orderRepository.save(existingOrder);
}
```

---

## 🎯 Casos de Uso Detalhados

### 🛒 **UC01: Fazer Pedido**

#### 📋 **Fluxo Principal**
1. **Cliente** (opcional) se identifica
2. **Sistema** valida cliente (se fornecido)
3. **Cliente** seleciona produtos e quantidades
4. **Sistema** valida produtos (existem e estão ativos)
5. **Sistema** calcula valor total
6. **Sistema** cria pedido com status RECEIVED
7. **Sistema** gera pagamento no Mercado Pago
8. **Sistema** retorna pedido com QR Code para pagamento

#### 💻 **Implementação**
```java
@Override
public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
    // 1. Validar cliente (opcional)
    Customer customer = findCustomerById(customerId);
    
    // 2. Validar e converter itens
    List<OrderItem> orderItems = validateAndConvertOrderItems(items);
    
    // 3. Criar pedido
    return createAndSaveOrder(customer, orderItems);
}

private List<OrderItem> validateAndConvertOrderItems(List<OrderItemRequest> items) {
    List<OrderItem> orderItems = new ArrayList<>();
    
    for (OrderItemRequest itemRequest : items) {
        // 🚫 REGRA: Quantidade deve ser positiva
        validateQuantity(itemRequest.getQuantity());
        
        // 🚫 REGRA: Produto deve existir e estar ativo
        Product product = validateProduct(itemRequest.getProductId());
        
        OrderItem orderItem = OrderItem.create(product, itemRequest.getQuantity());
        orderItems.add(orderItem);
    }
    
    return orderItems;
}

private void validateQuantity(Integer quantity) {
    if (quantity <= 0) {
        throw new DomainException("Quantity must be greater than zero");
    }
}

private Product validateProduct(UUID productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found"));

    // 🚫 REGRA DE NEGÓCIO: Produto deve estar ativo
    if (!product.isActive()) {
        throw new DomainException("Product is not active: " + product.getName());
    }

    return product;
}
```

#### 🚫 **Fluxos de Exceção**
- **Cliente não encontrado:** `NotFoundException`
- **Produto não encontrado:** `NotFoundException`
- **Produto inativo:** `DomainException`
- **Quantidade inválida:** `DomainException`

---

### 💳 **UC02: Processar Pagamento**

#### 📋 **Fluxo Principal**
1. **Mercado Pago** envia webhook de pagamento
2. **Sistema** recebe notificação com ID do pagamento
3. **Sistema** busca pedido pelo ID do pagamento
4. **Sistema** atualiza status do pagamento
5. **Se aprovado:** Sistema muda pedido para IN_PREPARATION
6. **Sistema** notifica cozinha (futuro)

#### 💻 **Implementação**
```java
// Webhook do Mercado Pago
@PostMapping("/webhook/payment")
public ResponseEntity<Void> receivePaymentNotification(@RequestBody WebhookRequestDTO request) {
    try {
        // Extrair ID do pagamento
        String paymentId = request.getData().getId();
        
        // Processar notificação
        paymentNotificationController.processPaymentNotification(paymentId);
        
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

// Use Case de processamento
@Override
public void processPaymentNotification(String paymentId) {
    try {
        Long id = Long.parseLong(paymentId);
        
        // 🚫 REGRA: Simular aprovação automática para demo
        // Em produção, consultaria API do Mercado Pago
        StatusPayment newStatus = StatusPayment.APROVADO;
        
        orderUseCase.updateOrderStatusPayment(id, newStatus);
        
    } catch (NumberFormatException e) {
        throw new DomainException("Invalid payment ID format: " + paymentId);
    }
}
```

#### 🔄 **Integração com Mercado Pago**
```java
// Conversão de status do Mercado Pago
public static StatusPayment fromMercadoPagoStatus(String mpStatus) {
    switch (mpStatus.toLowerCase()) {
        case "approved":
            return APROVADO;
        case "pending":
        case "in_process":
            return AGUARDANDO_PAGAMENTO;
        case "rejected":
        case "cancelled":
            return REJEITADO;
        default:
            throw new IllegalArgumentException("Status desconhecido: " + mpStatus);
    }
}
```

---

### 👨‍🍳 **UC03: Gerenciar Preparo**

#### 📋 **Fluxo da Cozinha**
1. **Cozinha** consulta pedidos em preparo
2. **Cozinheiro** inicia preparo do pedido
3. **Cozinheiro** finaliza preparo
4. **Sistema** muda status para READY
5. **Cliente** é notificado (futuro)

#### 💻 **Implementação**
```java
// Listar pedidos por status
@Override
public List<Order> findByOptionalStatus(OrderStatus status) {
    return orderRepository.findByOptionalStatus(status);
}

// Atualizar para "pronto"
@Override
public Order updateOrderStatus(Long id, OrderStatus status) {
    Order existingOrder = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Record not found"));

    // 🚫 REGRA: Só pode atualizar se pagamento aprovado
    if (!existingOrder.getStatusPayment().equals(StatusPayment.APROVADO)) {
        throw new DomainException("The order is not paid");
    }

    existingOrder.setStatus(status);
    existingOrder.setUpdatedAt(LocalDateTime.now());
    return orderRepository.save(existingOrder);
}
```

---

### 🛒 **UC04: Retirar Pedido**

#### 📋 **Fluxo de Retirada**
1. **Cliente** informa número do pedido
2. **Sistema** verifica se pedido está READY
3. **Atendente** entrega pedido
4. **Sistema** muda status para FINISHED

---

## 🚫 Regras de Negócio Críticas

### 📋 **Pedidos**

#### ✅ **Regras de Criação**
```java
// 1. Estado inicial sempre RECEIVED + AGUARDANDO_PAGAMENTO
public static Order create(Customer customer, List<OrderItem> items) {
    return new Order(
        null, 
        customer != null ? customer.getId() : null,
        customer, 
        items, 
        total,
        OrderStatus.RECEIVED,           // ✅ Sempre inicia assim
        StatusPayment.AGUARDANDO_PAGAMENTO, // ✅ Sempre inicia assim
        null, 
        LocalDateTime.now(), 
        LocalDateTime.now()
    );
}

// 2. Cliente é opcional (pedido anônimo)
private Customer findCustomerById(UUID customerId) {
    if (customerId == null) {
        return null; // ✅ Permite pedido anônimo
    }
    return customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
}

// 3. Valor total calculado automaticamente
BigDecimal total = items.stream()
        .map(OrderItem::getSubTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

#### 🚫 **Regras de Atualização**
```java
// 1. Só pode mudar status se pagamento aprovado
if (!existingOrder.getStatusPayment().equals(StatusPayment.APROVADO)) {
    throw new DomainException("The order is not paid");
}

// 2. Pagamento aprovado automaticamente inicia preparo
if (statusPayment == StatusPayment.APROVADO) {
    existingOrder.setStatus(OrderStatus.IN_PREPARATION);
}
```

### 📦 **Produtos**

#### ✅ **Regras de Validação**
```java
// 1. Produto deve existir
Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));

// 2. Produto deve estar ativo
if (!product.isActive()) {
    throw new DomainException("Product is not active: " + product.getName());
}

// 3. Quantidade deve ser positiva
if (quantity <= 0) {
    throw new DomainException("Quantity must be greater than zero");
}
```

### 📁 **Categorias**

#### 🚫 **Regras de Exclusão**
```java
// Não pode deletar categoria com produtos vinculados
List<Product> productsInCategory = productRepository.findByCategoryId(id);
if (!productsInCategory.isEmpty()) {
    throw new DomainException("Não é possível deletar a categoria pois ela está vinculada a um ou mais produtos");
}
```

#### ✅ **Regras de Criação**
```java
// Nome da categoria deve ser único
if (categoryRepository.existsByName(name)) {
    throw new DomainException("Category with name " + name + " already exists");
}
```

---

## 📊 Fluxo Completo - Exemplo Prático

### 🎬 **Cenário: João faz um pedido de hambúrguer**

#### **1️⃣ Preparação (Gerente)**
```bash
# Criar categoria
POST /api/categories
{"name": "Lanches"}
# Retorna: {"id": "cat-123", "name": "Lanches"}

# Criar produto
POST /api/products
{
  "name": "Hambúrguer Artesanal",
  "description": "Hambúrguer com carne 180g",
  "price": 25.90,
  "categoryId": "cat-123"
}
# Retorna: {"id": "prod-456", "name": "Hambúrguer Artesanal", ...}
```

#### **2️⃣ Cliente se cadastra (Opcional)**
```bash
POST /api/customers
{
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "12345678901"
}
# Retorna: {"id": "cust-789", "name": "João Silva", ...}
```

#### **3️⃣ João faz o pedido**
```bash
POST /api/orders
{
  "customerId": "cust-789",
  "items": [
    {
      "productId": "prod-456",
      "quantity": 2
    }
  ]
}

# Sistema processa:
# ✅ Valida cliente existe
# ✅ Valida produto existe e está ativo
# ✅ Calcula total: 2 × 25.90 = 51.80
# ✅ Cria pedido com status RECEIVED
# ✅ Gera pagamento no Mercado Pago
# ✅ Retorna pedido com QR Code

# Retorna:
{
  "id": 1,
  "customerId": "cust-789",
  "customer": {"name": "João Silva", ...},
  "items": [...],
  "totalAmount": 51.80,
  "status": "RECEIVED",
  "statusPayment": "AGUARDANDO_PAGAMENTO",
  "idPayment": 12345,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### **4️⃣ João paga via PIX**
```bash
# Mercado Pago envia webhook
POST /api/webhook/payment
{
  "data": {
    "id": "12345"
  }
}

# Sistema processa:
# ✅ Busca pedido pelo idPayment = 12345
# ✅ Atualiza statusPayment = APROVADO
# ✅ Automaticamente muda status = IN_PREPARATION
```

#### **5️⃣ Cozinha consulta pedidos**
```bash
GET /api/orders?status=IN_PREPARATION

# Retorna lista de pedidos em preparo:
[
  {
    "id": 1,
    "customer": {"name": "João Silva"},
    "items": [{"product": {"name": "Hambúrguer Artesanal"}, "quantity": 2}],
    "status": "IN_PREPARATION",
    "statusPayment": "APROVADO"
  }
]
```

#### **6️⃣ Cozinha finaliza preparo**
```bash
PUT /api/orders/1/status
{"status": "READY"}

# Sistema valida:
# ✅ Pedido existe
# ✅ Pagamento está aprovado
# ✅ Atualiza status para READY
```

#### **7️⃣ João retira o pedido**
```bash
PUT /api/orders/1/status
{"status": "FINISHED"}

# Pedido finalizado! 🎉
```

---

## 🧪 Testando as Regras de Negócio

### ✅ **Cenários de Sucesso**
```java
@Test
void shouldCreateOrderWithValidData() {
    // Given
    UUID customerId = UUID.randomUUID();
    List<OrderItemRequest> items = Arrays.asList(
        new OrderItemRequest(productId, 2)
    );
    
    // When
    Order result = orderUseCase.createOrder(customerId, items);
    
    // Then
    assertThat(result.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    assertThat(result.getStatusPayment()).isEqualTo(StatusPayment.AGUARDANDO_PAGAMENTO);
    assertThat(result.getTotalAmount()).isEqualTo(expectedTotal);
}
```

### 🚫 **Cenários de Erro**
```java
@Test
void shouldThrowExceptionWhenProductIsInactive() {
    // Given
    Product inactiveProduct = Product.builder()
        .id(productId)
        .name("Produto Inativo")
        .active(false) // ❌ Produto inativo
        .build();
    
    when(productRepository.findById(productId)).thenReturn(Optional.of(inactiveProduct));
    
    // When & Then
    assertThatThrownBy(() -> orderUseCase.createOrder(customerId, items))
        .isInstanceOf(DomainException.class)
        .hasMessage("Product is not active: Produto Inativo");
}

@Test
void shouldThrowExceptionWhenUpdatingUnpaidOrder() {
    // Given
    Order unpaidOrder = createOrderWithStatus(StatusPayment.AGUARDANDO_PAGAMENTO);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(unpaidOrder));
    
    // When & Then
    assertThatThrownBy(() -> orderUseCase.updateOrderStatus(orderId, OrderStatus.READY))
        .isInstanceOf(DomainException.class)
        .hasMessage("The order is not paid");
}
```

---

## 🎯 Exercícios Práticos

### 🏋️ **Exercício 1: Implementar Desconto**
Adicione regra de desconto de 10% para pedidos acima de R$ 50:

1. Modifique a entidade Order para calcular desconto
2. Implemente a regra no método `create()`
3. Adicione testes para validar o desconto
4. Atualize o endpoint para mostrar valor original e com desconto

### 🏋️ **Exercício 2: Limite de Quantidade**
Implemente limite máximo de 10 itens por produto:

1. Adicione validação no `validateQuantity()`
2. Crie exceção específica `QuantityExceededException`
3. Teste cenários válidos e inválidos
4. Documente a regra no Swagger

### 🏋️ **Exercício 3: Cancelamento de Pedido**
Permita cancelar pedidos não pagos:

1. Adicione status `CANCELLED` no enum
2. Implemente método `cancelOrder()` no Use Case
3. Regra: só pode cancelar se status = RECEIVED
4. Adicione endpoint DELETE /orders/{id}

---

## 🎓 Conclusão

As regras de negócio são o **coração** do sistema. Elas definem:

- **🎯 O que** o sistema faz
- **🚫 O que** não é permitido
- **🔄 Como** os dados fluem
- **⚡ Quando** as ações acontecem

**💡 Princípio Fundamental:** Regras de negócio devem estar no **domínio**, não espalhadas pelos controllers ou repositories.

---

**📚 Próximo Passo:** Leia o [⚡ TUTORIAL_QUICKSTART.md](./TUTORIAL_QUICKSTART.md) para colocar a mão na massa!