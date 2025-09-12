# 📋 Guia Completo da Clean Architecture

> **🎯 Objetivo:** Entender como a Clean Architecture foi implementada neste projeto e por que cada decisão arquitetural foi tomada.

---

## 🤔 Por que Clean Architecture?

### ❌ **Problemas da Arquitetura Tradicional**
```java
// ❌ Controlador acoplado ao banco de dados
@RestController
public class OrderController {
    @Autowired
    private OrderRepository repository; // Dependência direta!
    
    @PostMapping("/orders")
    public Order create(@RequestBody OrderRequest request) {
        // Lógica de negócio misturada com infraestrutura
        Order order = new Order();
        order.setStatus("RECEIVED");
        return repository.save(order); // E se mudar o banco?
    }
}
```

### ✅ **Solução com Clean Architecture**
```java
// ✅ Controlador desacoplado
@RestController
public class OrderRestController {
    private final OrderController orderController; // Adapter!
    
    @PostMapping("/orders")
    public ResponseEntity<Order> create(@RequestBody OrderRequestDTO request) {
        // Apenas converte e delega
        Order order = orderController.createOrder(request.getCustomerId(), request.getItems());
        return ResponseEntity.status(CREATED).body(order);
    }
}
```

**💡 Diferença:** O controlador REST não conhece banco de dados, apenas delega para a camada de aplicação.

---

## 🏛️ As 4 Camadas Explicadas

### 🎯 **1. DOMAIN (Núcleo do Sistema)**

**📍 Localização:** `src/main/java/com/fiap/techchallenge/domain/`

**🎯 Responsabilidade:** Regras de negócio puras, independentes de tecnologia.

#### 🏢 **Entities (Entidades)**
```java
// domain/entities/Order.java
public class Order {
    private Long id;
    private UUID customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private StatusPayment statusPayment;
    
    // ✅ Factory method - regra de negócio
    public static Order create(Customer customer, List<OrderItem> items) {
        BigDecimal total = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new Order(
            null, 
            customer != null ? customer.getId() : null,
            customer, 
            items, 
            total,
            OrderStatus.RECEIVED,           // ✅ Estado inicial definido aqui
            StatusPayment.AGUARDANDO_PAGAMENTO, // ✅ Regra de negócio
            null, 
            LocalDateTime.now(), 
            LocalDateTime.now()
        );
    }
}
```

**💡 Por que assim?** A entidade conhece suas próprias regras. O estado inicial de um pedido é sempre "RECEIVED" - isso é regra de negócio, não configuração.

#### 📝 **Repository Interfaces**
```java
// domain/repositories/OrderRepository.java
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByOptionalStatus(OrderStatus status);
    Optional<Order> findByIdPayment(Long idPayment);
}
```

**💡 Por que interface?** O domínio define O QUE precisa, não COMO será implementado. A implementação fica na camada externa.

#### ⚠️ **Domain Exceptions**
```java
// domain/exception/DomainException.java
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
```

**💡 Regra:** Exceções de domínio representam violações de regras de negócio, não problemas técnicos.

---

### 📋 **2. APPLICATION (Casos de Uso)**

**📍 Localização:** `src/main/java/com/fiap/techchallenge/application/`

**🎯 Responsabilidade:** Orquestrar as regras de negócio para casos de uso específicos.

#### 🎯 **Use Cases**
```java
// application/usecases/OrderUseCaseImpl.java
public class OrderUseCaseImpl implements OrderUseCase {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    
    @Override
    public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
        // ✅ 1. Validar cliente (se fornecido)
        Customer customer = findCustomerById(customerId);
        
        // ✅ 2. Validar e converter itens
        List<OrderItem> orderItems = validateAndConvertOrderItems(items);
        
        // ✅ 3. Criar e salvar pedido
        return createAndSaveOrder(customer, orderItems);
    }
    
    private List<OrderItem> validateAndConvertOrderItems(List<OrderItemRequest> items) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : items) {
            // ✅ Validação de regra de negócio
            validateQuantity(itemRequest.getQuantity());
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
}
```

**💡 Por que Use Case?** Cada caso de uso representa uma funcionalidade completa do sistema. É testável isoladamente e contém toda a lógica de orquestração.

#### 🔄 **Mappers**
```java
// application/usecases/mappers/OrderMapper.java
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
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

**💡 Por que Mapper?** Converte entre objetos de domínio e objetos de infraestrutura, mantendo o domínio limpo.

---

### 🔌 **3. ADAPTERS (Interface Adapters)**

**📍 Localização:** `src/main/java/com/fiap/techchallenge/adapters/`

**🎯 Responsabilidade:** Adaptar dados entre as camadas externa e de aplicação.

#### 🎮 **Controllers**
```java
// adapters/controllers/OrderController.java
public class OrderController {
    
    private final OrderUseCase orderUseCase;
    
    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }
    
    public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
        // ✅ Apenas delega para o Use Case
        return orderUseCase.createOrder(customerId, items);
    }
    
    public Optional<Order> findOrderById(Long id) {
        return orderUseCase.findOrderById(id);
    }
}
```

**💡 Por que Controller Adapter?** Isola a camada de aplicação dos detalhes do framework web. Se trocar Spring por outro framework, apenas esta camada muda.

#### 🚪 **Gateways**
```java
// adapters/gateway/OrderRepositoryGateway.java
public class OrderRepositoryGateway implements OrderRepository {
    
    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;
    
    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}
```

**💡 Por que Gateway?** Implementa as interfaces do domínio usando tecnologias específicas (JPA, MongoDB, etc.). O domínio não sabe que está usando JPA.

---

### 🌐 **4. EXTERNAL (Frameworks & Drivers)**

**📍 Localização:** `src/main/java/com/fiap/techchallenge/external/`

**🎯 Responsabilidade:** Implementações específicas de frameworks e integrações externas.

#### 🌐 **REST Controllers**
```java
// external/api/OrderRestController.java
@RestController
@RequestMapping("/orders")
public class OrderRestController {
    
    private final OrderController orderController;
    
    @PostMapping
    @Operation(summary = "Criar novo pedido")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        // ✅ 1. Converte DTO para tipos de domínio
        Order order = orderController.createOrder(
            orderRequest.getCustomerId(), 
            orderRequest.getItems()
        );
        
        // ✅ 2. Retorna resposta HTTP apropriada
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    // ✅ DTOs internos - detalhes de implementação
    public static class OrderRequestDTO {
        private UUID customerId;
        private List<OrderItemRequest> items;
        // getters/setters
    }
}
```

**💡 Por que REST Controller separado?** Lida apenas com detalhes HTTP: status codes, headers, serialização JSON. A lógica fica no adapter.

#### 🗄️ **JPA Entities**
```java
// external/datasource/entities/OrderJpaEntity.java
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItemJpaEntity> items;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatusJpa status;
    
    // ✅ Enum específico para JPA
    public enum OrderStatusJpa {
        RECEIVED, IN_PREPARATION, READY, FINISHED
    }
}
```

**💡 Por que JPA Entity separada?** Anotações JPA são detalhes de infraestrutura. A entidade de domínio fica limpa, sem dependências de framework.

---

## 🔄 Fluxo de Dados Completo

### 📊 **Criação de Pedido - Passo a Passo**

```
1. 🌐 HTTP Request
   POST /api/orders
   {"customerId": "123", "items": [...]}
   
2. 🌐 OrderRestController
   ├── Recebe OrderRequestDTO
   ├── Valida formato JSON
   └── Chama OrderController
   
3. 🔌 OrderController (Adapter)
   ├── Converte DTO para tipos de domínio
   └── Chama OrderUseCase
   
4. 📋 OrderUseCaseImpl
   ├── Valida regras de negócio
   ├── Busca Customer (se fornecido)
   ├── Valida Products
   ├── Cria Order (entidade de domínio)
   └── Chama OrderRepository.save()
   
5. 🔌 OrderRepositoryGateway
   ├── Converte Order para OrderJpaEntity
   ├── Chama OrderJpaRepository.save()
   └── Converte resultado de volta para Order
   
6. 🗄️ OrderJpaRepository
   ├── Executa SQL INSERT
   └── Retorna OrderJpaEntity salva
   
7. 🔄 Resposta (caminho inverso)
   OrderJpaEntity → Order → OrderController → OrderRestController → HTTP Response
```

### 💡 **Benefícios desta Arquitetura**

#### ✅ **Testabilidade**
```java
// Teste do Use Case - SEM banco de dados
@Test
void shouldCreateOrderSuccessfully() {
    // Given
    OrderRepository mockRepository = mock(OrderRepository.class);
    CustomerRepository mockCustomerRepo = mock(CustomerRepository.class);
    ProductRepository mockProductRepo = mock(ProductRepository.class);
    
    OrderUseCaseImpl useCase = new OrderUseCaseImpl(
        mockRepository, mockCustomerRepo, mockProductRepo, mockPaymentRepo
    );
    
    // When
    Order result = useCase.createOrder(customerId, items);
    
    // Then
    assertThat(result.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    verify(mockRepository).save(any(Order.class));
}
```

#### ✅ **Flexibilidade**
```java
// Trocar PostgreSQL por MongoDB? Apenas implemente a interface!
public class OrderMongoGateway implements OrderRepository {
    private final MongoTemplate mongoTemplate;
    
    @Override
    public Order save(Order order) {
        OrderDocument doc = OrderDocumentMapper.fromDomain(order);
        OrderDocument saved = mongoTemplate.save(doc);
        return OrderDocumentMapper.toDomain(saved);
    }
}
```

#### ✅ **Manutenibilidade**
- **Domínio:** Regras de negócio centralizadas
- **Use Cases:** Funcionalidades isoladas
- **Adapters:** Mudanças de framework não afetam o core
- **External:** Detalhes técnicos isolados

---

## 🧪 Testando Cada Camada

### 🎯 **Domain Layer**
```java
@Test
void shouldCreateOrderWithCorrectInitialState() {
    // Given
    Customer customer = new Customer(UUID.randomUUID(), "João", "joao@email.com");
    List<OrderItem> items = Arrays.asList(
        OrderItem.create(product1, 2),
        OrderItem.create(product2, 1)
    );
    
    // When
    Order order = Order.create(customer, items);
    
    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    assertThat(order.getStatusPayment()).isEqualTo(StatusPayment.AGUARDANDO_PAGAMENTO);
    assertThat(order.getTotalAmount()).isEqualTo(expectedTotal);
}
```

### 📋 **Application Layer**
```java
@Test
void shouldThrowExceptionWhenProductNotFound() {
    // Given
    when(productRepository.findById(productId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> orderUseCase.createOrder(customerId, items))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Product not found");
}
```

### 🔌 **Adapters Layer**
```java
@Test
void shouldDelegateToUseCase() {
    // Given
    when(orderUseCase.createOrder(customerId, items)).thenReturn(expectedOrder);
    
    // When
    Order result = orderController.createOrder(customerId, items);
    
    // Then
    assertThat(result).isEqualTo(expectedOrder);
    verify(orderUseCase).createOrder(customerId, items);
}
```

### 🌐 **External Layer**
```java
@WebMvcTest(OrderRestController.class)
class OrderRestControllerTest {
    
    @Test
    void shouldReturnCreatedWhenOrderIsValid() throws Exception {
        // Given
        when(orderController.createOrder(any(), any())).thenReturn(expectedOrder);
        
        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedOrder.getId()));
    }
}
```

---

## 🚫 Violações Comuns e Como Evitar

### ❌ **Violação 1: Domain conhece Infrastructure**
```java
// ❌ ERRADO - Entity com anotações JPA
@Entity // ❌ Dependência de framework no domínio!
public class Order {
    @Id
    @GeneratedValue
    private Long id;
}
```

### ✅ **Correção: Separar entidades**
```java
// ✅ CORRETO - Domain Entity limpa
public class Order {
    private Long id; // Sem anotações!
}

// ✅ JPA Entity separada
@Entity
public class OrderJpaEntity {
    @Id
    @GeneratedValue
    private Long id;
}
```

### ❌ **Violação 2: Use Case conhece HTTP**
```java
// ❌ ERRADO - Use Case retornando ResponseEntity
public ResponseEntity<Order> createOrder(...) { // ❌ HTTP no Use Case!
    Order order = // lógica
    return ResponseEntity.ok(order);
}
```

### ✅ **Correção: Use Case retorna domínio**
```java
// ✅ CORRETO - Use Case retorna entidade de domínio
public Order createOrder(...) {
    // lógica de negócio
    return order; // Apenas domínio!
}
```

---

## 🎯 Exercícios Práticos

### 🏋️ **Exercício 1: Adicionar Validação**
Implemente validação de email no Customer:

1. Adicione método `isValidEmail()` na entidade Customer
2. Use a validação no CustomerUseCase
3. Teste a validação isoladamente
4. Verifique se a exceção é tratada corretamente

### 🏋️ **Exercício 2: Novo Status de Pedido**
Adicione status "CANCELLED":

1. Modifique o enum OrderStatus
2. Implemente regra: só pode cancelar se status for RECEIVED
3. Adicione endpoint PUT /orders/{id}/cancel
4. Teste todas as camadas

### 🏋️ **Exercício 3: Trocar Banco de Dados**
Implemente OrderRepository usando HashMap (em memória):

1. Crie OrderInMemoryGateway
2. Implemente todos os métodos da interface
3. Configure no Spring para usar em testes
4. Verifique se os Use Cases continuam funcionando

---

## 🎓 Conclusão

A Clean Architecture pode parecer complexa no início, mas os benefícios são enormes:

- **🧪 Testabilidade:** Cada camada testada isoladamente
- **🔄 Flexibilidade:** Trocar tecnologias sem afetar o core
- **📈 Manutenibilidade:** Código organizado e responsabilidades claras
- **👥 Colaboração:** Equipes podem trabalhar em camadas diferentes

**💡 Lembre-se:** A arquitetura não é sobre pastas ou frameworks. É sobre **separar o que importa (regras de negócio) do que não importa (detalhes técnicos)**.

---

**📚 Próximo Passo:** Leia o [🚀 GUIA_NEGOCIO.md](./GUIA_NEGOCIO.md) para entender as regras de negócio implementadas neste sistema.