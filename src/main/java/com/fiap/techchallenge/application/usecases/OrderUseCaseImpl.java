package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.*;
import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.repositories.CustomerRepository;
import com.fiap.techchallenge.domain.repositories.OrderRepository;
import com.fiap.techchallenge.domain.repositories.PaymentRepository;
import com.fiap.techchallenge.domain.repositories.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderUseCaseImpl implements OrderUseCase {

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
        Customer customer = findCustomerById(customerId);
        List<OrderItem> orderItems = validateAndConvertOrderItems(items);
        return createAndSaveOrder(customer, orderItems);
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
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
        }
        return order;
    }

    @Override
    public List<Order> findByOptionalStatus(OrderStatus status) {
        return orderRepository.findByOptionalStatus(status);
    }

    @Override
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

        if (!existingOrder.getStatusPayment().equals(StatusPayment.APROVADO)) {
            throw new DomainException("The order is not paid");
        }

        existingOrder.setStatus(status);
        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }

    @Override
    public Order updateOrderStatus(Long id) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

        existingOrder.setStatus(OrderStatus.IN_PREPARATION);
        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }

    @Override
    public Order updateOrderStatusPayment(Long id, StatusPayment statusPayment) {
        Order existingOrder = orderRepository.findByIdPayment(id)
                .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

        existingOrder.setStatusPayment(statusPayment);
        existingOrder.setStatus(OrderStatus.IN_PREPARATION);
        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }
}
