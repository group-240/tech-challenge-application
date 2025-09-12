package com.fiap.techchallenge.external.datasource.mercadopago;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.domain.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.UUID;

@Component
public class MercadoPagoClientImpl implements MercadoPagoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mercado-pago.access-token}")
    private String accessToken;

    @Override
    public Long createPaymentOrder(
        Double amount,
        String description,
        String paymentMethodId,
        Integer installments,
        String payerEmail,
        String identificationType,
        String identificationNumber
    ) {

        String url = "https://api.mercadopago.com/v1/payments";
        String notificationUrl = "https://example.com/notify";

        // Configura os headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        // Monta o corpo da requisição (JSON)
        StringBuilder requestBody = new StringBuilder("{");
        requestBody.append(String.format(Locale.US, "\"transaction_amount\":%.2f,", amount));
        requestBody.append(String.format("\"description\":\"%s\",", description));
        requestBody.append(String.format("\"payment_method_id\":\"%s\",", paymentMethodId));
        requestBody.append(String.format("\"installments\":%d", installments));
        requestBody.append(",");
        //requestBody.append(String.format("\"token\":\"%s\",", accessToken));
        requestBody.append("\"payer\":{");
        requestBody.append(String.format("\"email\":\"%s\"", "brunoaugustoloc@gmail.com"));
        if (identificationType != null && identificationNumber != null) {
            requestBody.append(",");
            requestBody.append(String.format("\"identification\":{\"type\":\"%s\",\"number\":\"%s\"}", identificationType, identificationNumber));
        }
        requestBody.append("}");
        requestBody.append(String.format(",\"notification_url\":\"%s\"", notificationUrl));
        requestBody.append("}");

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        try {
            String response = restTemplate.exchange(url, HttpMethod.POST, request, String.class).getBody();
            return getPaymentId(response);
        } catch (Exception e) {
            throw new DomainException("Error in createPaymentOrder: " + e.getMessage());
        }
    }

    private static Long getPaymentId(String response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new DomainException("Error in getPaymentId: " + e.getMessage());
        }

        return root.path("id").asLong();
    }
}
