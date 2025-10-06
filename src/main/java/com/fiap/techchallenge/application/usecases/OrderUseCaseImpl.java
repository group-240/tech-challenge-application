package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.*;
import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.repositories.CustomerRepository;
import com.fiap.techchallenge.domain.repositories.OrderRepository;
import com.fiap.techchallenge.domain.repositories.PaymentRepository;
import com.fiap.techchallenge.domain.repositories.ProductRepository;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderUseCaseImpl implements OrderUseCase {

    private static final Logger logger = LoggerFactory.getLogger(OrderUseCaseImpl.class);
    private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public OrderUseCaseImpl(OrderRepository orderRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Order createOrder(UUID customerId, List<OrderItemRequest> items) {
        long startTime = System.currentTimeMillis();
        
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("CreateOrder");
            if (customerId != null) {
                StructuredLogger.setCustomerId(customerId.toString());
            }
            
            logger.info("Order creation started: items={}", items.size());
            
            Customer customer = findCustomerById(customerId);
            List<OrderItem> orderItems = validateAndConvertOrderItems(items);
            Order order = createAndSaveOrder(customer, orderItems);
            
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            StructuredLogger.setOrderId(order.getId().toString());
            StructuredLogger.put("totalAmount", order.getTotalAmount().toString());
            
            logger.info("Order created successfully: orderId={}, totalAmount={}, items={}", 
                       order.getId(), order.getTotalAmount(), orderItems.size());
            
            return order;
            
        } catch (NotFoundException e) {
            logger.warn("Order creation failed - resource not found: {}", e.getMessage());
            throw e;
        } catch (DomainException e) {
            logger.warn("Order creation failed - validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("ORDER_CREATION_FAILED", e.getMessage());
            logger.error("Failed to create order", e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    private Customer findCustomerById(UUID customerId) {
        if (customerId == null) {
            return null;
        }
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

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

    private Product validateProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new DomainException("Product is not active: " + product.getName());
        }

        return product;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
    }

    private Order createAndSaveOrder(Customer customer, List<OrderItem> orderItems) {
        Order order = Order.create(customer, orderItems);
        order.setStatus(OrderStatus.RECEIVED);
        order.setStatusPayment(StatusPayment.AGUARDANDO_PAGAMENTO);
        order.setIdPayment(createPaymentOrder(order, customer));

        return orderRepository.save(order);
    }

    private Long createPaymentOrder(Order order, Customer customer) {

        Double amount = order.getTotalAmount().doubleValue();
        String description = "Pagamento para o pedido";
        String paymentMethodId = "pix";
        Integer installments = 1;
        String emailPayment= null;
        String identificationType = "CPF";
        String cpfPayment = null;

        if (customer != null) {
            emailPayment = customer.getEmail();
            cpfPayment = customer.getCpf();
        }

        return paymentRepository.createPaymentOrder(amount, description, paymentMethodId, installments,
                emailPayment, identificationType, cpfPayment);

    }

    @Override
    public Optional<Order> findOrderById(Long id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindOrderById");
            StructuredLogger.setOrderId(id.toString());
            
            Optional<Order> order = orderRepository.findById(id);
            if (order.isEmpty()) {
                logger.warn("Order not found: orderId={}", id);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }
            
            logger.info("Order found: orderId={}, status={}", id, order.get().getStatus());
            return order;
            
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("ORDER_FIND_FAILED", e.getMessage());
            logger.error("Failed to find order: orderId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public List<Order> findByOptionalStatus(OrderStatus status) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindOrdersByStatus");
            if (status != null) {
                StructuredLogger.put("status", status.name());
            }
            
            List<Order> orders = orderRepository.findByOptionalStatus(status);
            logger.info("Orders found: status={}, count={}", status, orders.size());
            
            return orders;
            
        } catch (Exception e) {
            StructuredLogger.setError("ORDER_LIST_FAILED", e.getMessage());
            logger.error("Failed to list orders by status: status={}", status, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Order updateOrderStatus(Long id, OrderStatus status) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("UpdateOrderStatus");
            StructuredLogger.setOrderId(id.toString());
            StructuredLogger.put("newStatus", status.name());
            
            Order existingOrder = orderRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

            if (!existingOrder.getStatusPayment().equals(StatusPayment.APROVADO)) {
                logger.warn("Order status update failed - payment not approved: orderId={}, paymentStatus={}", 
                           id, existingOrder.getStatusPayment());
                throw new DomainException("The order is not paid");
            }

            OrderStatus oldStatus = existingOrder.getStatus();
            existingOrder.setStatus(status);
            existingOrder.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(existingOrder);
            
            logger.info("Order status updated: orderId={}, oldStatus={}, newStatus={}", 
                       id, oldStatus, status);
            
            return updatedOrder;
            
        } catch (NotFoundException e) {
            logger.warn("Order not found for status update: orderId={}", id);
            throw e;
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("ORDER_STATUS_UPDATE_FAILED", e.getMessage());
            logger.error("Failed to update order status: orderId={}, newStatus={}", id, status, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Order updateOrderStatus(Long id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("UpdateOrderToInPreparation");
            StructuredLogger.setOrderId(id.toString());
            
            Order existingOrder = orderRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

            OrderStatus oldStatus = existingOrder.getStatus();
            existingOrder.setStatus(OrderStatus.IN_PREPARATION);
            existingOrder.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(existingOrder);
            
            logger.info("Order moved to preparation: orderId={}, oldStatus={}", id, oldStatus);
            
            return updatedOrder;
            
        } catch (NotFoundException e) {
            logger.warn("Order not found for status update: orderId={}", id);
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("ORDER_STATUS_UPDATE_FAILED", e.getMessage());
            logger.error("Failed to update order to preparation: orderId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Order updateOrderStatusPayment(Long id, StatusPayment statusPayment) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("UpdateOrderPaymentStatus");
            StructuredLogger.setPaymentId(id.toString());
            StructuredLogger.put("paymentStatus", statusPayment.name());
            
            Order existingOrder = orderRepository.findByIdPayment(id)
                    .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

            StatusPayment oldPaymentStatus = existingOrder.getStatusPayment();
            existingOrder.setStatusPayment(statusPayment);
            existingOrder.setStatus(OrderStatus.IN_PREPARATION);
            existingOrder.setUpdatedAt(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(existingOrder);
            
            logger.info("Order payment status updated: paymentId={}, orderId={}, oldPaymentStatus={}, newPaymentStatus={}", 
                       id, existingOrder.getId(), oldPaymentStatus, statusPayment);
            
            return updatedOrder;
            
        } catch (NotFoundException e) {
            logger.warn("Order not found for payment update: paymentId={}", id);
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("ORDER_PAYMENT_UPDATE_FAILED", e.getMessage());
            logger.error("Failed to update order payment status: paymentId={}, paymentStatus={}", 
                        id, statusPayment, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }
}
