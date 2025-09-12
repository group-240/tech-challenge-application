package com.fiap.techchallenge.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order {
    private Long id;
    private UUID customerId;
    private Customer customer;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private StatusPayment statusPayment;
    private Long idPayment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {}

    public Order(Long id, UUID customerId, Customer customer, List<OrderItem> items,
                 BigDecimal totalAmount, OrderStatus status, StatusPayment statusPayment,
                 Long idPayment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.customer = customer;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.statusPayment = statusPayment;
        this.idPayment = idPayment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory method
    public static Order create(Customer customer, List<OrderItem> items) {
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
            now,
            now
        );
    }

    // Getters
    public Long getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public StatusPayment getStatusPayment() { return statusPayment; }
    public Long getIdPayment() { return idPayment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setStatusPayment(StatusPayment statusPayment) { this.statusPayment = statusPayment; }
    public void setIdPayment(Long idPayment) { this.idPayment = idPayment; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
