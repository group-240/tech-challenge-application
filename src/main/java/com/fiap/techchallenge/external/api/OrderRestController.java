package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.adapters.controllers.OrderController;
import com.fiap.techchallenge.application.usecases.OrderUseCase.OrderItemRequest;
import com.fiap.techchallenge.domain.entities.Order;
import com.fiap.techchallenge.domain.entities.OrderStatus;
import com.fiap.techchallenge.domain.entities.StatusPayment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "API para gerenciamento de pedidos")
public class OrderRestController {

    private final OrderController orderController;

    public OrderRestController(OrderController orderController) {
        this.orderController = orderController;
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        Order order = orderController.createOrder(orderRequest.getCustomerId(), orderRequest.getItems());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    public ResponseEntity<Order> findOrderById(@PathVariable Long id) {
        return orderController.findOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar pedidos por status")
    public ResponseEntity<List<Order>> findByOptionalStatus(@RequestParam(required = false) OrderStatus status) {
        List<Order> orders = orderController.findByOptionalStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido",
               description = "Status possíveis: RECEIVED, IN_PREPARATION, READY, FINISHED")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateDTO statusUpdate) {
        Order order = orderController.updateOrderStatus(id, statusUpdate.getStatus());
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status/preparation")
    @Operation(summary = "Atualizar pedido para preparo")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id) {
        Order order = orderController.updateOrderStatus(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/payment-status")
    @Operation(summary = "Atualizar status de pagamento",
              description = "Status possíveis: AGUARDANDO_PAGAMENTO, APROVADO, REJEITADO")
    public ResponseEntity<Order> updateOrderStatusPayment(@PathVariable Long id, @RequestBody PaymentStatusUpdateDTO paymentUpdate) {
        Order order = orderController.updateOrderStatusPayment(id, paymentUpdate.getStatusPayment());
        return ResponseEntity.ok(order);
    }

    // DTOs internos
    public static class OrderRequestDTO {
        private UUID customerId;
        private List<OrderItemRequest> items;

        public UUID getCustomerId() { return customerId; }
        public void setCustomerId(UUID customerId) { this.customerId = customerId; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class OrderStatusUpdateDTO {
        private OrderStatus status;

        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
    }

    public static class PaymentStatusUpdateDTO {
        private StatusPayment statusPayment;

        public StatusPayment getStatusPayment() { return statusPayment; }
        public void setStatusPayment(StatusPayment statusPayment) { this.statusPayment = statusPayment; }
    }
}
