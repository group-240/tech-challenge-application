package com.fiap.techchallenge.external.datasource.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private UUID customerId;

    @ManyToOne
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private CustomerJpaEntity customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<OrderItemJpaEntity> items;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatusJpa status;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_payment", nullable = false)
    private StatusPaymentJpa statusPayment;

    @Column(name = "id_payment")
    private Long idPayment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrderJpaEntity() {}

    // Enums JPA específicos para persistência
    public enum OrderStatusJpa {
        RECEIVED, IN_PREPARATION, READY, FINISHED
    }

    public enum StatusPaymentJpa {
        AGUARDANDO_PAGAMENTO, APROVADO, REJEITADO
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public CustomerJpaEntity getCustomer() { return customer; }
    public void setCustomer(CustomerJpaEntity customer) { this.customer = customer; }
    public List<OrderItemJpaEntity> getItems() { return items; }
    public void setItems(List<OrderItemJpaEntity> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatusJpa getStatus() { return status; }
    public void setStatus(OrderStatusJpa status) { this.status = status; }
    public StatusPaymentJpa getStatusPayment() { return statusPayment; }
    public void setStatusPayment(StatusPaymentJpa statusPayment) { this.statusPayment = statusPayment; }
    public Long getIdPayment() { return idPayment; }
    public void setIdPayment(Long idPayment) { this.idPayment = idPayment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
