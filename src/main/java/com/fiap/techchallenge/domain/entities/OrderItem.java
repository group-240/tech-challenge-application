package com.fiap.techchallenge.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private UUID id;
    private UUID productId;
    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;

    public OrderItem() {}

    public OrderItem(UUID id, UUID productId, Product product, Integer quantity, BigDecimal unitPrice, BigDecimal subTotal) {
        this.id = id;
        this.productId = productId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }

    // Factory method
    public static OrderItem create(Product product, Integer quantity) {
        BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        return new OrderItem(
            null,
            product.getId(),
            product,
            quantity,
            product.getPrice(),
            subTotal
        );
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubTotal() { return subTotal; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
}
