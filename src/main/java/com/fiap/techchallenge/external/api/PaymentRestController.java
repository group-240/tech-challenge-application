package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.adapters.controllers.PaymentController;
import com.fiap.techchallenge.external.api.dto.PaymentOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Tag(name = "Payments", description = "API para pagamento")
public class PaymentRestController {

    private final PaymentController paymentController;

    public PaymentRestController(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @PostMapping("/create-order")
    @Operation(summary = "Cria uma ordem de pagamento no Mercado Pago")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordem de pagamento criada com sucesso", content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "400", description = "Erro na requisição, como parâmetros inválidos", content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> createOrder(@Valid @RequestBody PaymentOrderRequest request) {
        try {
            if (request.getAmount() == null || request.getDescription() == null ||
                request.getPaymentMethodId() == null || request.getInstallments() == null ||
                request.getPayerEmail() == null) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Parâmetros obrigatórios ausentes: amount, description, paymentMethodId, installments, payerEmail");
            }

            Long result = paymentController.createPaymentOrder(
                    request.getAmount(),
                    request.getDescription(),
                    request.getPaymentMethodId(),
                    request.getInstallments(),
                    request.getPayerEmail(),
                    request.getIdentificationType(),
                    request.getIdentificationNumber()
            );

            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro na requisição: " + e.getMessage());
        }
    }
}
