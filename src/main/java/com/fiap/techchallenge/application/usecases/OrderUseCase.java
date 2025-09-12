package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Order;
import com.fiap.techchallenge.domain.entities.OrderStatus;
import com.fiap.techchallenge.domain.entities.StatusPayment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderUseCase {

    public static class OrderItemRequest {
        private UUID productId;
        private Integer quantity;

        public OrderItemRequest() {}

        public OrderItemRequest(UUID productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    Order createOrder(UUID customerId, List<OrderItemRequest> items);
    Optional<Order> findOrderById(Long id);
    List<Order> findByOptionalStatus(OrderStatus status);
    Order updateOrderStatus(Long id, OrderStatus status);
    Order updateOrderStatus(Long id);
    Order updateOrderStatusPayment(Long id, StatusPayment statusPayment);
}
