package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.adapters.controllers.WebhookController;
import com.fiap.techchallenge.external.api.dto.WebhookRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhooks", description = "API para receber notificações de pagamento")
public class WebhookRestController {

    private final WebhookController webhookController;

    public WebhookRestController(WebhookController webhookController) {
        this.webhookController = webhookController;
    }

    @PostMapping
    @Operation(
            summary = "Receber notificação de pagamento",
            description = "Recebe notificações de pagamento de gateways externos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificação processada com sucesso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido",
                    content = @Content
            )
    })
    public ResponseEntity<Void> handleNotification(
            @Valid @RequestBody
            @Parameter(description = "Payload da notificação de pagamento", required = true)
            WebhookRequestDTO notificationRequest) {

        webhookController.handlePaymentNotification(notificationRequest.getData().getId());
        return ResponseEntity.ok().build();
    }
}
