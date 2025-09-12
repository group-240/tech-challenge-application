package com.fiap.techchallenge.external.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para verificação de saúde da aplicação
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "API para monitoramento da saúde da aplicação")
public class HealthCheckRestController {

    @GetMapping
    @Operation(
        summary = "Verificar saúde da aplicação",
        description = "Retorna o status atual da aplicação e informações de disponibilidade"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Aplicação está funcionando corretamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Object.class))
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "tech-challenge");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
