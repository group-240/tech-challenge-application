package com.fiap.techchallenge.application.usecases.mappers;

import com.fiap.techchallenge.domain.entities.*;
import com.fiap.techchallenge.external.datasource.entities.*;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderJpaEntity toJpaEntity(Order order) {
        if (order == null) return null;

        OrderJpaEntity jpaEntity = new OrderJpaEntity();
        jpaEntity.setId(order.getId());
        jpaEntity.setCustomerId(order.getCustomerId());

        if (order.getCustomer() != null) {
            jpaEntity.setCustomer(CustomerMapper.toJpaEntity(order.getCustomer()));
        }

        if (order.getItems() != null) {
            List<OrderItemJpaEntity> itemsJpa = order.getItems().stream()
                .map(OrderMapper::toJpaOrderItem)
                .collect(Collectors.toList());
            jpaEntity.setItems(itemsJpa);
        }

        jpaEntity.setTotalAmount(order.getTotalAmount());
        jpaEntity.setStatus(mapToJpaStatus(order.getStatus()));
        jpaEntity.setStatusPayment(mapToJpaPaymentStatus(order.getStatusPayment()));
        jpaEntity.setIdPayment(order.getIdPayment());
        jpaEntity.setCreatedAt(order.getCreatedAt());
        jpaEntity.setUpdatedAt(order.getUpdatedAt());

        return jpaEntity;
    }

    public static Order toDomainEntity(OrderJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Customer customer = null;
        if (jpaEntity.getCustomer() != null) {
            customer = CustomerMapper.toDomainEntity(jpaEntity.getCustomer());
        }

        List<OrderItem> items = null;
        if (jpaEntity.getItems() != null) {
            items = jpaEntity.getItems().stream()
                .map(OrderMapper::toDomainOrderItem)
                .collect(Collectors.toList());
        }

        return new Order(
            jpaEntity.getId(),
            jpaEntity.getCustomerId(),
            customer,
            items,
            jpaEntity.getTotalAmount(),
            mapToDomainStatus(jpaEntity.getStatus()),
            mapToDomainPaymentStatus(jpaEntity.getStatusPayment()),
            jpaEntity.getIdPayment(),
            jpaEntity.getCreatedAt(),
            jpaEntity.getUpdatedAt()
        );
    }

    private static OrderItemJpaEntity toJpaOrderItem(OrderItem orderItem) {
        if (orderItem == null) return null;

        OrderItemJpaEntity jpaEntity = new OrderItemJpaEntity();
        jpaEntity.setId(orderItem.getId());
        jpaEntity.setProductId(orderItem.getProductId());

        if (orderItem.getProduct() != null) {
            jpaEntity.setProduct(ProductMapper.toJpaEntity(orderItem.getProduct()));
        }

        jpaEntity.setQuantity(orderItem.getQuantity());
        jpaEntity.setUnitPrice(orderItem.getUnitPrice());
        jpaEntity.setSubTotal(orderItem.getSubTotal());

        return jpaEntity;
    }

    private static OrderItem toDomainOrderItem(OrderItemJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Product product = null;
        if (jpaEntity.getProduct() != null) {
            product = ProductMapper.toDomainEntity(jpaEntity.getProduct());
        }

        return new OrderItem(
            jpaEntity.getId(),
            jpaEntity.getProductId(),
            product,
            jpaEntity.getQuantity(),
            jpaEntity.getUnitPrice(),
            jpaEntity.getSubTotal()
        );
    }

    // Status mappers
    private static OrderJpaEntity.OrderStatusJpa mapToJpaStatus(OrderStatus status) {
        if (status == null) return null;
        switch (status) {
            case RECEIVED: return OrderJpaEntity.OrderStatusJpa.RECEIVED;
            case IN_PREPARATION: return OrderJpaEntity.OrderStatusJpa.IN_PREPARATION;
            case READY: return OrderJpaEntity.OrderStatusJpa.READY;
            case FINISHED: return OrderJpaEntity.OrderStatusJpa.FINISHED;
            default: throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private static OrderStatus mapToDomainStatus(OrderJpaEntity.OrderStatusJpa jpaStatus) {
        if (jpaStatus == null) return null;
        switch (jpaStatus) {
            case RECEIVED: return OrderStatus.RECEIVED;
            case IN_PREPARATION: return OrderStatus.IN_PREPARATION;
            case READY: return OrderStatus.READY;
            case FINISHED: return OrderStatus.FINISHED;
            default: throw new IllegalArgumentException("Unknown JPA status: " + jpaStatus);
        }
    }

    private static OrderJpaEntity.StatusPaymentJpa mapToJpaPaymentStatus(StatusPayment status) {
        if (status == null) return null;
        switch (status) {
            case AGUARDANDO_PAGAMENTO: return OrderJpaEntity.StatusPaymentJpa.AGUARDANDO_PAGAMENTO;
            case APROVADO: return OrderJpaEntity.StatusPaymentJpa.APROVADO;
            case REJEITADO: return OrderJpaEntity.StatusPaymentJpa.REJEITADO;
            default: throw new IllegalArgumentException("Unknown payment status: " + status);
        }
    }

    private static StatusPayment mapToDomainPaymentStatus(OrderJpaEntity.StatusPaymentJpa jpaStatus) {
        if (jpaStatus == null) return null;
        switch (jpaStatus) {
            case AGUARDANDO_PAGAMENTO: return StatusPayment.AGUARDANDO_PAGAMENTO;
            case APROVADO: return StatusPayment.APROVADO;
            case REJEITADO: return StatusPayment.REJEITADO;
            default: throw new IllegalArgumentException("Unknown JPA payment status: " + jpaStatus);
        }
    }
}
