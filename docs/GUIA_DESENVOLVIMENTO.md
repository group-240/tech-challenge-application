# 🔧 Guia de Desenvolvimento - Como Estudar e Modificar o Código

> **🎯 Objetivo:** Aprender como navegar, entender, modificar e contribuir com o código seguindo as boas práticas de Clean Architecture.

---

## 📚 Como Estudar o Código

### 🗺️ **Roteiro de Estudo Recomendado**

#### **📍 Nível 1: Entendendo a Estrutura (30 min)**
```
1. 📁 Explore a estrutura de pastas
   └── src/main/java/com/fiap/techchallenge/
       ├── 🎯 domain/          ← Comece aqui!
       ├── 📋 application/     ← Depois aqui
       ├── 🔌 adapters/        ← Em seguida
       └── 🌐 external/        ← Por último

2. 🔍 Leia as entidades de domínio
   ├── Category.java
   ├── Product.java
   ├── Customer.java
   ├── Order.java
   └── OrderItem.java

3. 📋 Entenda os contratos (interfaces)
   └── domain/repositories/
```

#### **📍 Nível 2: Seguindo um Fluxo (45 min)**
```
Escolha: "Criar Categoria" e siga o fluxo completo:

🌐 CategoryRestController.java
    ↓ (chama)
🔌 CategoryController.java  
    ↓ (chama)
📋 CategoryUseCaseImpl.java
    ↓ (usa)
🔌 CategoryRepositoryGateway.java
    ↓ (implementa)
🗄️ CategoryJpaRepository.java
```

#### **📍 Nível 3: Analisando Regras de Negócio (60 min)**
```
1. 🎯 Leia OrderUseCaseImpl.java linha por linha
2. 🔍 Identifique todas as validações
3. 📊 Entenda como o status do pedido muda
4. 💳 Analise a integração com pagamento
```

### 🔍 **Técnicas de Análise**

#### **🕵️ Método "Seguir o Rastro"**
```java
// 1. Comece com um endpoint
@PostMapping("/orders")
public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDTO request) {
    // 2. Veja para onde ele chama
    Order order = orderController.createOrder(request.getCustomerId(), request.getItems());
    // 3. Vá para o OrderController e continue seguindo...
}
```

#### **🧩 Método "Quebra-Cabeça"**
```java
// 1. Escolha uma entidade (ex: Order)
public class Order {
    // 2. Veja quais campos ela tem
    private OrderStatus status;
    private StatusPayment statusPayment;
    
    // 3. Procure onde esses campos são modificados
    // 4. Entenda as regras por trás de cada modificação
}
```

#### **🔄 Método "Fluxo Reverso"**
```java
// 1. Comece com uma exceção
throw new DomainException("The order is not paid");

// 2. Procure onde ela é lançada
// 3. Entenda que condição causa essa exceção
// 4. Trace o caminho até o endpoint que pode gerar isso
```

---

## 🧪 Estrutura de Testes

### 📊 **Pirâmide de Testes Implementada**

```
        🔺 E2E Tests
       /   (Poucos)    \
      /                 \
     🔺 Integration Tests 🔺
    /     (Alguns)        \
   /                       \
  🔺🔺🔺 Unit Tests 🔺🔺🔺
     (Muitos - Cada camada)
```

### 🎯 **Testes por Camada**

#### **🏢 Domain Layer Tests**
```java
// Teste de entidade - Regras de negócio puras
class OrderTest {
    
    @Test
    @DisplayName("Deve criar pedido com estado inicial correto")
    void shouldCreateOrderWithCorrectInitialState() {
        // Given
        Customer customer = Customer.builder()
            .id(UUID.randomUUID())
            .name("João")
            .email("joao@email.com")
            .build();
            
        Product product = Product.builder()
            .id(UUID.randomUUID())
            .name("Hambúrguer")
            .price(BigDecimal.valueOf(25.90))
            .build();
            
        List<OrderItem> items = Arrays.asList(
            OrderItem.create(product, 2)
        );
        
        // When
        Order order = Order.create(customer, items);
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(order.getStatusPayment()).isEqualTo(StatusPayment.AGUARDANDO_PAGAMENTO);
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(51.80));
        assertThat(order.getCustomerId()).isEqualTo(customer.getId());
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getCreatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Deve permitir pedido sem cliente (anônimo)")
    void shouldAllowOrderWithoutCustomer() {
        // Given
        List<OrderItem> items = Arrays.asList(
            OrderItem.create(product, 1)
        );
        
        // When
        Order order = Order.create(null, items);
        
        // Then
        assertThat(order.getCustomerId()).isNull();
        assertThat(order.getCustomer()).isNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }
}
```

#### **📋 Application Layer Tests**
```java
// Teste de Use Case - Lógica de aplicação
class OrderUseCaseImplTest {
    
    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PaymentRepository paymentRepository;
    
    @InjectMocks private OrderUseCaseImpl orderUseCase;
    
    @Test
    @DisplayName("Deve criar pedido com dados válidos")
    void shouldCreateOrderWithValidData() {
        // Given
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        
        Customer customer = Customer.builder()
            .id(customerId)
            .name("João")
            .build();
            
        Product product = Product.builder()
            .id(productId)
            .name("Hambúrguer")
            .price(BigDecimal.valueOf(25.90))
            .active(true)
            .build();
            
        List<OrderItemRequest> items = Arrays.asList(
            new OrderItemRequest(productId, 2)
        );
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(paymentRepository.createPaymentOrder(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(12345L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        
        // When
        Order result = orderUseCase.createOrder(customerId, items);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(result.getStatusPayment()).isEqualTo(StatusPayment.AGUARDANDO_PAGAMENTO);
        assertThat(result.getIdPayment()).isEqualTo(12345L);
        
        verify(orderRepository).save(any(Order.class));
        verify(paymentRepository).createPaymentOrder(
            eq(51.80), 
            eq("Pagamento para o pedido"), 
            eq("pix"), 
            eq(1),
            eq("joao@email.com"),
            eq("CPF"),
            any()
        );
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        UUID productId = UUID.randomUUID();
        List<OrderItemRequest> items = Arrays.asList(
            new OrderItemRequest(productId, 1)
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> orderUseCase.createOrder(null, items))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Product not found");
            
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando produto inativo")
    void shouldThrowExceptionWhenProductInactive() {
        // Given
        UUID productId = UUID.randomUUID();
        Product inactiveProduct = Product.builder()
            .id(productId)
            .name("Produto Inativo")
            .active(false)
            .build();
            
        List<OrderItemRequest> items = Arrays.asList(
            new OrderItemRequest(productId, 1)
        );
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(inactiveProduct));
        
        // When & Then
        assertThatThrownBy(() -> orderUseCase.createOrder(null, items))
            .isInstanceOf(DomainException.class)
            .hasMessage("Product is not active: Produto Inativo");
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando quantidade inválida")
    void shouldThrowExceptionWhenInvalidQuantity() {
        // Given
        UUID productId = UUID.randomUUID();
        List<OrderItemRequest> items = Arrays.asList(
            new OrderItemRequest(productId, 0) // Quantidade inválida
        );
        
        // When & Then
        assertThatThrownBy(() -> orderUseCase.createOrder(null, items))
            .isInstanceOf(DomainException.class)
            .hasMessage("Quantity must be greater than zero");
    }
}
```

#### **🔌 Adapters Layer Tests**
```java
// Teste de Controller - Orquestração
class OrderControllerTest {
    
    @Mock private OrderUseCase orderUseCase;
    @InjectMocks private OrderController orderController;
    
    @Test
    @DisplayName("Deve delegar criação para Use Case")
    void shouldDelegateCreationToUseCase() {
        // Given
        UUID customerId = UUID.randomUUID();
        List<OrderItemRequest> items = Arrays.asList(
            new OrderItemRequest(UUID.randomUUID(), 1)
        );
        
        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        
        when(orderUseCase.createOrder(customerId, items)).thenReturn(expectedOrder);
        
        // When
        Order result = orderController.createOrder(customerId, items);
        
        // Then
        assertThat(result).isEqualTo(expectedOrder);
        verify(orderUseCase).createOrder(customerId, items);
    }
}
```

#### **🌐 External Layer Tests**
```java
// Teste de REST Controller - HTTP
@WebMvcTest(OrderRestController.class)
class OrderRestControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @MockBean private OrderController orderController;
    
    @Test
    @DisplayName("Deve retornar 201 quando pedido criado com sucesso")
    void shouldReturn201WhenOrderCreatedSuccessfully() throws Exception {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.RECEIVED);
        order.setTotalAmount(BigDecimal.valueOf(25.90));
        
        when(orderController.createOrder(any(), any())).thenReturn(order);
        
        String requestJson = """
            {
                "customerId": null,
                "items": [
                    {
                        "productId": "550e8400-e29b-41d4-a716-446655440000",
                        "quantity": 1
                    }
                ]
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.totalAmount").value(25.90));
    }
    
    @Test
    @DisplayName("Deve retornar 400 quando dados inválidos")
    void shouldReturn400WhenInvalidData() throws Exception {
        // Given
        String invalidRequestJson = """
            {
                "customerId": null,
                "items": []
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }
}
```

### 🏃‍♂️ **Executando os Testes**

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=OrderUseCaseImplTest

# Testes com cobertura
mvn test jacoco:report

# Ver relatório de cobertura
open target/site/jacoco/index.html
```

---

## 🛠️ Como Adicionar Novas Funcionalidades

### 🎯 **Exemplo Prático: Adicionar Campo "Observações" no Pedido**

#### **1️⃣ Domain Layer (Regras de Negócio)**
```java
// 1. Modificar entidade Order
public class Order {
    private Long id;
    private UUID customerId;
    private Customer customer;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private StatusPayment statusPayment;
    private Long idPayment;
    private String observations; // ✅ NOVO CAMPO
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Atualizar construtor
    public Order(Long id, UUID customerId, Customer customer, List<OrderItem> items,
                 BigDecimal totalAmount, OrderStatus status, StatusPayment statusPayment,
                 Long idPayment, String observations, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.customer = customer;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.statusPayment = statusPayment;
        this.idPayment = idPayment;
        this.observations = observations; // ✅ NOVO
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Factory method atualizado
    public static Order create(Customer customer, List<OrderItem> items, String observations) {
        BigDecimal total = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();

        return new Order(
            null,
            customer != null ? customer.getId() : null,
            customer,
            items,
            total,
            OrderStatus.RECEIVED,
            StatusPayment.AGUARDANDO_PAGAMENTO,
            null,
            observations, // ✅ NOVO
            now,
            now
        );
    }
    
    // Getter e Setter
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
```

#### **2️⃣ Application Layer (Use Cases)**
```java
// 2. Atualizar interface OrderUseCase
public interface OrderUseCase {
    Order createOrder(UUID customerId, List<OrderItemRequest> items, String observations);
    // outros métodos...
}

// 3. Atualizar implementação
public class OrderUseCaseImpl implements OrderUseCase {
    
    @Override
    public Order createOrder(UUID customerId, List<OrderItemRequest> items, String observations) {
        Customer customer = findCustomerById(customerId);
        List<OrderItem> orderItems = validateAndConvertOrderItems(items);
        
        // ✅ Validar observações (regra de negócio)
        validateObservations(observations);
        
        return createAndSaveOrder(customer, orderItems, observations);
    }
    
    private void validateObservations(String observations) {
        if (observations != null && observations.length() > 500) {
            throw new DomainException("Observations cannot exceed 500 characters");
        }
    }
    
    private Order createAndSaveOrder(Customer customer, List<OrderItem> orderItems, String observations) {
        Order order = Order.create(customer, orderItems, observations);
        order.setStatus(OrderStatus.RECEIVED);
        order.setStatusPayment(StatusPayment.AGUARDANDO_PAGAMENTO);
        order.setIdPayment(createPaymentOrder(order, customer));

        return orderRepository.save(order);
    }
}
```

#### **3️⃣ Adapters Layer (Controllers)**
```java
// 4. Atualizar OrderController
public class OrderController {
    
    public Order createOrder(UUID customerId, List<OrderItemRequest> items, String observations) {
        return orderUseCase.createOrder(customerId, items, observations);
    }
}
```

#### **4️⃣ External Layer (REST e JPA)**
```java
// 5. Atualizar DTO
public static class OrderRequestDTO {
    private UUID customerId;
    private List<OrderItemRequest> items;
    private String observations; // ✅ NOVO
    
    // getters/setters
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}

// 6. Atualizar REST Controller
@PostMapping
@Operation(summary = "Criar novo pedido")
public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDTO orderRequest) {
    Order order = orderController.createOrder(
        orderRequest.getCustomerId(), 
        orderRequest.getItems(),
        orderRequest.getObservations() // ✅ NOVO
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(order);
}

// 7. Atualizar JPA Entity
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    // outros campos...
    
    @Column(name = "observations", length = 500)
    private String observations; // ✅ NOVO
    
    // getter/setter
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}

// 8. Atualizar Mapper
public class OrderMapper {
    
    public static Order toDomain(OrderJpaEntity entity) {
        if (entity == null) return null;
        
        return new Order(
            entity.getId(),
            entity.getCustomerId(),
            CustomerMapper.toDomain(entity.getCustomer()),
            entity.getItems().stream()
                .map(OrderItemMapper::toDomain)
                .collect(Collectors.toList()),
            entity.getTotalAmount(),
            OrderStatus.valueOf(entity.getStatus().name()),
            StatusPayment.valueOf(entity.getStatusPayment().name()),
            entity.getIdPayment(),
            entity.getObservations(), // ✅ NOVO
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    public static OrderJpaEntity toJpaEntity(Order domain) {
        if (domain == null) return null;
        
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(domain.getId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setStatus(OrderJpaEntity.OrderStatusJpa.valueOf(domain.getStatus().name()));
        entity.setStatusPayment(OrderJpaEntity.StatusPaymentJpa.valueOf(domain.getStatusPayment().name()));
        entity.setIdPayment(domain.getIdPayment());
        entity.setObservations(domain.getObservations()); // ✅ NOVO
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        
        return entity;
    }
}
```

#### **5️⃣ Testes**
```java
// 9. Atualizar testes
@Test
@DisplayName("Deve criar pedido com observações")
void shouldCreateOrderWithObservations() {
    // Given
    String observations = "Sem cebola, por favor";
    
    // When
    Order result = orderUseCase.createOrder(customerId, items, observations);
    
    // Then
    assertThat(result.getObservations()).isEqualTo(observations);
}

@Test
@DisplayName("Deve lançar exceção quando observações muito longas")
void shouldThrowExceptionWhenObservationsTooLong() {
    // Given
    String longObservations = "a".repeat(501); // 501 caracteres
    
    // When & Then
    assertThatThrownBy(() -> orderUseCase.createOrder(customerId, items, longObservations))
        .isInstanceOf(DomainException.class)
        .hasMessage("Observations cannot exceed 500 characters");
}
```

#### **6️⃣ Migração do Banco**
```sql
-- 10. Script de migração
ALTER TABLE orders 
ADD COLUMN observations VARCHAR(500);
```

### ✅ **Checklist para Nova Funcionalidade**

- [ ] **Domain:** Entidade atualizada com regras de negócio
- [ ] **Application:** Use Case implementado com validações
- [ ] **Adapters:** Controller atualizado
- [ ] **External:** REST endpoint e JPA entity atualizados
- [ ] **Mappers:** Conversões entre camadas
- [ ] **Testes:** Cobertura completa de todos os cenários
- [ ] **Migração:** Script de banco de dados
- [ ] **Documentação:** Swagger atualizado

---

## 🎨 Boas Práticas Implementadas

### 🏗️ **Arquitetura**

#### ✅ **Dependency Inversion**
```java
// ✅ CORRETO - Use Case depende de abstração
public class OrderUseCaseImpl implements OrderUseCase {
    private final OrderRepository orderRepository; // Interface!
    
    public OrderUseCaseImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}

// ✅ CORRETO - Gateway implementa a interface
public class OrderRepositoryGateway implements OrderRepository {
    private final OrderJpaRepository jpaRepository; // Implementação específica
}
```

#### ✅ **Single Responsibility**
```java
// ✅ CORRETO - Cada classe tem uma responsabilidade
public class OrderController {
    // Responsabilidade: Orquestrar chamadas para Use Cases
}

public class OrderUseCaseImpl {
    // Responsabilidade: Implementar regras de negócio de pedidos
}

public class OrderRepositoryGateway {
    // Responsabilidade: Persistir dados usando JPA
}
```

#### ✅ **Open/Closed Principle**
```java
// ✅ CORRETO - Aberto para extensão, fechado para modificação
public interface PaymentRepository {
    Long createPaymentOrder(Double amount, String description, ...);
}

// Implementação atual: Mercado Pago
public class MercadoPagoPaymentGateway implements PaymentRepository {
    // Implementação específica do Mercado Pago
}

// Futura implementação: PayPal (sem modificar código existente)
public class PayPalPaymentGateway implements PaymentRepository {
    // Nova implementação sem afetar o resto do sistema
}
```

### 🧪 **Testes**

#### ✅ **Arrange-Act-Assert (AAA)**
```java
@Test
void shouldCreateOrderSuccessfully() {
    // Arrange (Given)
    UUID customerId = UUID.randomUUID();
    List<OrderItemRequest> items = Arrays.asList(
        new OrderItemRequest(productId, 2)
    );
    
    // Act (When)
    Order result = orderUseCase.createOrder(customerId, items);
    
    // Assert (Then)
    assertThat(result.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    assertThat(result.getTotalAmount()).isEqualTo(expectedTotal);
}
```

#### ✅ **Test Naming Convention**
```java
// ✅ CORRETO - Nome descritivo
@Test
@DisplayName("Deve lançar exceção quando produto não encontrado")
void shouldThrowExceptionWhenProductNotFound() {
    // teste...
}

// ✅ CORRETO - Cenário específico
@Test
@DisplayName("Deve permitir pedido anônimo sem cliente")
void shouldAllowAnonymousOrderWithoutCustomer() {
    // teste...
}
```

### 💻 **Código Limpo**

#### ✅ **Métodos Pequenos e Focados**
```java
// ✅ CORRETO - Método faz uma coisa só
private void validateQuantity(Integer quantity) {
    if (quantity <= 0) {
        throw new DomainException("Quantity must be greater than zero");
    }
}

// ✅ CORRETO - Método com nome descritivo
private List<OrderItem> validateAndConvertOrderItems(List<OrderItemRequest> items) {
    List<OrderItem> orderItems = new ArrayList<>();
    
    for (OrderItemRequest itemRequest : items) {
        validateQuantity(itemRequest.getQuantity());
        Product product = validateProduct(itemRequest.getProductId());
        OrderItem orderItem = OrderItem.create(product, itemRequest.getQuantity());
        orderItems.add(orderItem);
    }
    
    return orderItems;
}
```

#### ✅ **Constantes Bem Definidas**
```java
// ✅ CORRETO - Constantes com nomes descritivos
private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found";
private static final String PRODUCT_NOT_ACTIVE_MESSAGE = "Product is not active: ";
private static final int MAX_OBSERVATION_LENGTH = 500;
```

#### ✅ **Tratamento de Exceções**
```java
// ✅ CORRETO - Exceções específicas do domínio
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

// ✅ CORRETO - Uso apropriado das exceções
if (!product.isActive()) {
    throw new DomainException("Product is not active: " + product.getName());
}
```

---

## 🐛 Debugging e Profiling

### 🔍 **Técnicas de Debug**

#### **📊 Logs Estruturados**
```java
// ✅ Adicionar logs informativos
@Override
public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
    log.info("Creating order for customer: {} with {} items", customerId, items.size());
    
    try {
        Customer customer = findCustomerById(customerId);
        log.debug("Customer found: {}", customer != null ? customer.getName() : "Anonymous");
        
        List<OrderItem> orderItems = validateAndConvertOrderItems(items);
        log.debug("Order items validated: {} items", orderItems.size());
        
        Order order = createAndSaveOrder(customer, orderItems);
        log.info("Order created successfully with ID: {}", order.getId());
        
        return order;
    } catch (Exception e) {
        log.error("Error creating order for customer: {}", customerId, e);
        throw e;
    }
}
```

#### **🔧 Debug Points Estratégicos**
```java
// Pontos importantes para breakpoints:

// 1. Entrada dos Use Cases
public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
    // 🔴 BREAKPOINT AQUI - Ver parâmetros de entrada
    
// 2. Validações críticas
private void validateQuantity(Integer quantity) {
    // 🔴 BREAKPOINT AQUI - Ver se validação está funcionando
    if (quantity <= 0) {

// 3. Chamadas para repositórios
Order savedOrder = orderRepository.save(order);
// 🔴 BREAKPOINT AQUI - Ver se dados estão sendo salvos

// 4. Retorno dos métodos
return order;
// 🔴 BREAKPOINT AQUI - Ver estado final do objeto
```

### 📊 **Monitoramento de Performance**

#### **⏱️ Métricas Customizadas**
```java
@Component
public class OrderMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter orderCreatedCounter;
    private final Timer orderCreationTimer;
    
    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderCreatedCounter = Counter.builder("orders.created")
            .description("Number of orders created")
            .register(meterRegistry);
        this.orderCreationTimer = Timer.builder("orders.creation.time")
            .description("Time to create an order")
            .register(meterRegistry);
    }
    
    public void incrementOrderCreated() {
        orderCreatedCounter.increment();
    }
    
    public Timer.Sample startOrderCreationTimer() {
        return Timer.start(meterRegistry);
    }
}

// Uso no Use Case
@Override
public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
    Timer.Sample sample = orderMetrics.startOrderCreationTimer();
    
    try {
        Order order = // lógica de criação
        
        orderMetrics.incrementOrderCreated();
        return order;
    } finally {
        sample.stop(orderMetrics.getOrderCreationTimer());
    }
}
```

#### **📈 Acessar Métricas**
```bash
# Métricas da aplicação
curl http://localhost:8080/api/actuator/prometheus

# Métricas específicas
curl http://localhost:8080/api/actuator/metrics/orders.created
curl http://localhost:8080/api/actuator/metrics/orders.creation.time
```

---

## 🎯 Exercícios Práticos

### 🏋️ **Exercício 1: Implementar Desconto por Quantidade**
**Objetivo:** Adicionar desconto de 5% para pedidos com mais de 5 itens.

**Passos:**
1. Modifique a entidade Order para calcular desconto
2. Implemente a regra no método create()
3. Adicione testes unitários
4. Atualize o endpoint para mostrar desconto aplicado

**Dica:** Comece pelo domínio, depois suba as camadas.

### 🏋️ **Exercício 2: Histórico de Status**
**Objetivo:** Manter histórico de mudanças de status do pedido.

**Passos:**
1. Crie entidade OrderStatusHistory
2. Implemente Use Case para registrar mudanças
3. Adicione endpoint GET /orders/{id}/history
4. Teste o fluxo completo

### 🏋️ **Exercício 3: Validação de CPF**
**Objetivo:** Validar CPF do cliente usando algoritmo real.

**Passos:**
1. Implemente validação na entidade Customer
2. Adicione testes para CPFs válidos e inválidos
3. Teste via API
4. Documente a validação no Swagger

### 🏋️ **Exercício 4: Cache de Produtos**
**Objetivo:** Implementar cache para consultas de produtos.

**Passos:**
1. Adicione dependência do Redis
2. Implemente ProductCacheGateway
3. Configure TTL apropriado
4. Teste performance com e sem cache

---

## 🎓 Conclusão

**🎉 Parabéns!** Agora você sabe como:

- ✅ **Navegar** no código seguindo a arquitetura
- ✅ **Entender** as regras de negócio implementadas
- ✅ **Testar** cada camada isoladamente
- ✅ **Adicionar** novas funcionalidades
- ✅ **Debugar** problemas eficientemente
- ✅ **Monitorar** performance da aplicação

**💡 Lembre-se:** Clean Architecture é sobre **separar responsabilidades**. Cada camada tem seu papel específico e deve ser testada isoladamente.

**🚀 Próximo nível:** Agora você está pronto para contribuir com projetos reais usando Clean Architecture!

---

**📚 Continue aprendendo:** Explore o [📊 GUIA_MONITORAMENTO.md](./docs/GUIA_MONITORAMENTO.md) para entender observabilidade completa.