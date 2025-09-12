package com.fiap.techchallenge.adapters.controllers;

import com.fiap.techchallenge.application.usecases.OrderUseCase;
import com.fiap.techchallenge.application.usecases.OrderUseCase.OrderItemRequest;
import com.fiap.techchallenge.domain.entities.Order;
import com.fiap.techchallenge.domain.entities.OrderStatus;
import com.fiap.techchallenge.domain.entities.StatusPayment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
        return orderUseCase.createOrder(customerId, items);
    }

    public Optional<Order> findOrderById(Long id) {
        return orderUseCase.findOrderById(id);
    }

    public List<Order> findByOptionalStatus(OrderStatus status) {
        return orderUseCase.findByOptionalStatus(status);
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        return orderUseCase.updateOrderStatus(id, status);
    }

    public Order updateOrderStatus(Long id) {
        return orderUseCase.updateOrderStatus(id);
    }

    public Order updateOrderStatusPayment(Long id, StatusPayment statusPayment) {
        return orderUseCase.updateOrderStatusPayment(id, statusPayment);
    }
}
