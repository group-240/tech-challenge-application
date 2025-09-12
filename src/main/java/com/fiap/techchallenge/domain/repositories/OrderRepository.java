package com.fiap.techchallenge.domain.repositories;

import com.fiap.techchallenge.domain.entities.Order;
import com.fiap.techchallenge.domain.entities.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByOptionalStatus(OrderStatus status);
    List<Order> findAll();
    boolean existsByProductId(UUID productId);
    Optional<Order>  findByIdPayment(Long id);
}
