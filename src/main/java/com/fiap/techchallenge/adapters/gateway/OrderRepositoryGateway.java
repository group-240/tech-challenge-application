package com.fiap.techchallenge.adapters.gateway;

import com.fiap.techchallenge.application.usecases.mappers.OrderMapper;
import com.fiap.techchallenge.domain.entities.Order;
import com.fiap.techchallenge.domain.entities.OrderStatus;
import com.fiap.techchallenge.domain.repositories.OrderRepository;
import com.fiap.techchallenge.external.datasource.entities.OrderJpaEntity;
import com.fiap.techchallenge.external.datasource.repositories.OrderJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderRepositoryGateway implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryGateway(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        var jpaEntity = OrderMapper.toJpaEntity(order);
        var savedEntity = orderJpaRepository.save(jpaEntity);
        return OrderMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
                .map(OrderMapper::toDomainEntity);
    }

    @Override
    public Optional<Order> findByIdPayment(Long id) {
        return orderJpaRepository.findByIdPayment(id)
                .map(OrderMapper::toDomainEntity);
    }

    @Override
    public List<Order> findByOptionalStatus(OrderStatus status) {
        OrderJpaEntity.OrderStatusJpa jpaStatus = null;
        if (status != null) {
            jpaStatus = mapToJpaStatus(status);
        }
        return orderJpaRepository.findByOptionalStatus(jpaStatus)
                .stream()
                .map(OrderMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll()
                .stream()
                .map(OrderMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByProductId(UUID productId) {
        return orderJpaRepository.existsByItemsProductId(productId);
    }

    private OrderJpaEntity.OrderStatusJpa mapToJpaStatus(OrderStatus status) {
        switch (status) {
            case RECEIVED: return OrderJpaEntity.OrderStatusJpa.RECEIVED;
            case IN_PREPARATION: return OrderJpaEntity.OrderStatusJpa.IN_PREPARATION;
            case READY: return OrderJpaEntity.OrderStatusJpa.READY;
            case FINISHED: return OrderJpaEntity.OrderStatusJpa.FINISHED;
            default: throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
}
