package com.fiap.techchallenge.external.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@lombok.Data
public class Data {

    @Schema(description = "Id do pagamento", example = "9988776655")
    private Long id;
}
