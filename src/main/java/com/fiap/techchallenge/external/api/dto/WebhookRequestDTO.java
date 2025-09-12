package com.fiap.techchallenge.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO para receber notificações de pagamento via webhook")
public class WebhookRequestDTO {


    @Schema(description = "ID único do webhook", example = "1234567890")
    private Long id;

    @Schema(description = "Indica se o ambiente é de produção", example = "true")
    @JsonProperty("live_mode")
    private boolean liveMode;

    @Schema(description = "Tipo do recurso notificado", example = "payment")
    private String type;

    @Schema(description = "Ação executada no recurso", example = "payment.created")
    private String action;

    @Schema(description = "Data de criação do evento", example = "2025-08-01T19:40:00Z")
    @JsonProperty("date_created")
    private String dateCreated;

    @Schema(description = "ID do usuário que gerou o evento", example = "987654321")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Versão da API utilizada", example = "v1")
    @JsonProperty("api_version")
    private String apiVersion;

    @Schema(description = "Dados específicos do evento")
    private com.fiap.techchallenge.external.api.dto.Data data;
}
