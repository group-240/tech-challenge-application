package com.fiap.techchallenge.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utilitário para logging estruturado com contexto adicional.
 * Utiliza MDC (Mapped Diagnostic Context) para adicionar campos aos logs JSON.
 */
@Component
public class StructuredLogger {

    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String ORDER_ID = "orderId";
    private static final String CUSTOMER_ID = "customerId";
    private static final String PAYMENT_ID = "paymentId";

    /**
     * Gera e adiciona um correlation ID ao contexto de log.
     * Útil para rastrear uma requisição através de múltiplos serviços.
     */
    public static String generateCorrelationId() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);
        return correlationId;
    }

    /**
     * Adiciona o correlation ID ao contexto de log.
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isEmpty()) {
            MDC.put(CORRELATION_ID, correlationId);
        }
    }

    /**
     * Adiciona o user ID ao contexto de log.
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            MDC.put(USER_ID, userId);
        }
    }

    /**
     * Adiciona o order ID ao contexto de log.
     */
    public static void setOrderId(String orderId) {
        if (orderId != null && !orderId.isEmpty()) {
            MDC.put(ORDER_ID, orderId);
        }
    }

    /**
     * Adiciona o customer ID ao contexto de log.
     */
    public static void setCustomerId(String customerId) {
        if (customerId != null && !customerId.isEmpty()) {
            MDC.put(CUSTOMER_ID, customerId);
        }
    }

    /**
     * Adiciona o payment ID ao contexto de log.
     */
    public static void setPaymentId(String paymentId) {
        if (paymentId != null && !paymentId.isEmpty()) {
            MDC.put(PAYMENT_ID, paymentId);
        }
    }

    /**
     * Remove todos os campos do contexto de log.
     * IMPORTANTE: Sempre chamar no finally ou após processar a requisição.
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Remove um campo específico do contexto de log.
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * Adiciona um campo customizado ao contexto de log.
     */
    public static void put(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * Exemplo de uso em um caso de uso
     */
    public static class Example {
        private static final Logger logger = LoggerFactory.getLogger(Example.class);

        public void processOrder(String orderId, String customerId) {
            try {
                // Adicionar contexto
                StructuredLogger.generateCorrelationId();
                StructuredLogger.setOrderId(orderId);
                StructuredLogger.setCustomerId(customerId);

                logger.info("Processing order started");

                // Seu código aqui...
                // Todos os logs dentro deste escopo terão orderId e customerId

                logger.info("Order validated successfully");
                logger.info("Payment processed");
                logger.info("Order completed successfully");

            } catch (Exception e) {
                logger.error("Failed to process order", e);
                throw e;
            } finally {
                // SEMPRE limpar o contexto no final
                StructuredLogger.clear();
            }
        }
    }
}
