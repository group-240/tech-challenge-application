package com.fiap.techchallenge.external.api;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para o controller de verificação de saúde da aplicação
 * <p>
 * Esta classe testa o funcionamento do endpoint de health check,
 * verificando se ele retorna o status correto e as informações esperadas.
 * </p>
 */
@WebMvcTest(HealthCheckRestController.class)
public class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Testa se o endpoint de health check retorna status 200 (OK)
     * e as informações esperadas no corpo da resposta.
     */
    @Test
    @DisplayName("Deve retornar status OK e informações de saúde da aplicação")
    public void healthCheckShouldReturnOk() throws Exception {
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("tech-challenge"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
