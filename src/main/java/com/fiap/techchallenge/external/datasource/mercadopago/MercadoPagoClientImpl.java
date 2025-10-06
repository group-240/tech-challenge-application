package com.fiap.techchallenge.external.datasource.mercadopago;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.UUID;

@Component
public class MercadoPagoClientImpl implements MercadoPagoClient {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoClientImpl.class);
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
        long startTime = System.currentTimeMillis();

        try {
            StructuredLogger.setCategory(LogCategory.INTEGRATION);
            StructuredLogger.setOperation("CreateMercadoPagoPayment");
            StructuredLogger.put("amount", String.valueOf(amount));
            StructuredLogger.put("paymentMethod", paymentMethodId);
            
            logger.info("MercadoPago payment creation started: amount={}, paymentMethod={}", 
                       amount, paymentMethodId);
            
            String url = "https://api.mercadopago.com/v1/payments";
            String notificationUrl = "https://example.com/notify";

            // Configura os headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            String idempotencyKey = UUID.randomUUID().toString();
            headers.set("X-Idempotency-Key", idempotencyKey);
            
            StructuredLogger.put("idempotencyKey", idempotencyKey);

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

            String response = restTemplate.exchange(url, HttpMethod.POST, request, String.class).getBody();
            Long paymentId = getPaymentId(response);
            
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            StructuredLogger.setPaymentId(paymentId.toString());
            
            if (duration > 3000) {
                logger.warn("MercadoPago payment creation slow: paymentId={}, duration={}ms", 
                           paymentId, duration);
            } else {
                logger.info("MercadoPago payment created successfully: paymentId={}, duration={}ms", 
                           paymentId, duration);
            }
            
            return paymentId;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            StructuredLogger.setError("MERCADOPAGO_PAYMENT_FAILED", e.getMessage());
            logger.error("Failed to create MercadoPago payment: amount={}, duration={}ms", 
                        amount, duration, e);
            throw new DomainException("Error in createPaymentOrder: " + e.getMessage());
        } finally {
            StructuredLogger.clear();
        }
    }

    private static Long getPaymentId(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("id").asLong();
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse MercadoPago response", e);
            throw new DomainException("Error in getPaymentId: " + e.getMessage());
        }
    }
}
