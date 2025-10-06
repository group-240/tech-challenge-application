package com.fiap.techchallenge.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utilitário para logging estruturado seguindo práticas SRE (Site Reliability Engineering).
 * 
 * Utiliza MDC (Mapped Diagnostic Context) para adicionar campos aos logs JSON.
 * Todos os campos MDC são automaticamente incluídos em cada log.
 * 
 * Padrões SRE implementados:
 * - Correlation ID para rastreamento end-to-end
 * - Categorização de logs (BUSINESS, SECURITY, AUDIT, etc.)
 * - Campos contextuais para análise
 * - Suporte a métricas de performance
 */
@Component
public class StructuredLogger {

    // ========================================================================
    // CONSTANTES - Nomes dos campos MDC
    // ========================================================================
    
    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String ORDER_ID = "orderId";
    private static final String CUSTOMER_ID = "customerId";
    private static final String PAYMENT_ID = "paymentId";
    private static final String PRODUCT_ID = "productId";
    private static final String CATEGORY_ID = "categoryId";
    private static final String LOG_CATEGORY = "log_category";
    private static final String OPERATION = "operation";
    private static final String DURATION_MS = "duration_ms";
    private static final String HTTP_STATUS = "http_status";
    private static final String HTTP_METHOD = "http_method";
    private static final String ENDPOINT = "endpoint";
    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_MESSAGE = "error_message";

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
     * Adiciona o product ID ao contexto de log.
     */
    public static void setProductId(String productId) {
        if (productId != null && !productId.isEmpty()) {
            MDC.put(PRODUCT_ID, productId);
        }
    }

    /**
     * Adiciona o category ID ao contexto de log.
     */
    public static void setCategoryId(String categoryId) {
        if (categoryId != null && !categoryId.isEmpty()) {
            MDC.put(CATEGORY_ID, categoryId);
        }
    }

    // ========================================================================
    // MÉTODOS SRE - Categorização e Contexto
    // ========================================================================

    /**
     * Define a categoria do log (BUSINESS, SECURITY, AUDIT, etc.).
     * Facilita filtragem e análise de logs específicos.
     */
    public static void setCategory(String category) {
        if (category != null && !category.isEmpty()) {
            MDC.put(LOG_CATEGORY, category);
        }
    }

    /**
     * Define a operação sendo executada (CreateOrder, ProcessPayment, etc.).
     */
    public static void setOperation(String operation) {
        if (operation != null && !operation.isEmpty()) {
            MDC.put(OPERATION, operation);
        }
    }

    /**
     * Registra a duração de uma operação em milissegundos.
     * Útil para análise de performance.
     */
    public static void setDuration(long durationMs) {
        MDC.put(DURATION_MS, String.valueOf(durationMs));
    }

    /**
     * Registra informações HTTP da requisição.
     */
    public static void setHttpInfo(String method, String endpoint, int status) {
        if (method != null) MDC.put(HTTP_METHOD, method);
        if (endpoint != null) MDC.put(ENDPOINT, endpoint);
        MDC.put(HTTP_STATUS, String.valueOf(status));
    }

    /**
     * Registra informações de erro estruturadas.
     */
    public static void setError(String errorCode, String errorMessage) {
        if (errorCode != null) MDC.put(ERROR_CODE, errorCode);
        if (errorMessage != null) MDC.put(ERROR_MESSAGE, errorMessage);
    }

    // ========================================================================
    // MÉTODOS UTILITÁRIOS
    // ========================================================================

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

    // ========================================================================
    // EXEMPLOS DE USO SEGUINDO PRÁTICAS SRE
    // ========================================================================

    /**
     * Exemplo 1: Log de operação de negócio bem-sucedida (INFO + BUSINESS)
     */
    public static class BusinessOperationExample {
        private static final Logger logger = LoggerFactory.getLogger(BusinessOperationExample.class);

        public void createOrder(String orderId, String customerId, double amount) {
            long startTime = System.currentTimeMillis();
            try {
                // Contexto SRE
                StructuredLogger.generateCorrelationId();
                StructuredLogger.setCategory(LogCategory.BUSINESS);
                StructuredLogger.setOperation("CreateOrder");
                StructuredLogger.setOrderId(orderId);
                StructuredLogger.setCustomerId(customerId);
                StructuredLogger.put("amount", String.valueOf(amount));

                // INFO: Operação iniciada
                logger.info("Order creation started");

                // Lógica de negócio...
                
                // INFO: Operação concluída com sucesso
                long duration = System.currentTimeMillis() - startTime;
                StructuredLogger.setDuration(duration);
                logger.info("Order created successfully");

            } catch (Exception e) {
                // ERROR: Falha na operação (com stack trace)
                StructuredLogger.setError("ORDER_CREATION_FAILED", e.getMessage());
                logger.error("Failed to create order", e);
                throw e;
            } finally {
                StructuredLogger.clear();
            }
        }
    }

    /**
     * Exemplo 2: Log de evento de segurança (WARN + SECURITY)
     */
    public static class SecurityEventExample {
        private static final Logger logger = LoggerFactory.getLogger(SecurityEventExample.class);

        public void handleUnauthorizedAccess(String userId, String resource) {
            try {
                StructuredLogger.generateCorrelationId();
                StructuredLogger.setCategory(LogCategory.SECURITY);
                StructuredLogger.setUserId(userId);
                StructuredLogger.put("resource", resource);
                StructuredLogger.put("action", "ACCESS_DENIED");

                // WARN: Tentativa de acesso não autorizado (não é erro, é esperado)
                logger.warn("Unauthorized access attempt detected");

            } finally {
                StructuredLogger.clear();
            }
        }
    }

    /**
     * Exemplo 3: Log de integração com API externa (INFO/WARN/ERROR + INTEGRATION)
     */
    public static class IntegrationExample {
        private static final Logger logger = LoggerFactory.getLogger(IntegrationExample.class);

        public void callExternalAPI(String apiName, String endpoint) {
            long startTime = System.currentTimeMillis();
            try {
                StructuredLogger.generateCorrelationId();
                StructuredLogger.setCategory(LogCategory.INTEGRATION);
                StructuredLogger.setOperation("CallExternalAPI");
                StructuredLogger.put("api_name", apiName);
                StructuredLogger.put("endpoint", endpoint);

                // INFO: Chamada iniciada
                logger.info("External API call started");

                // Simular chamada...
                int status = 200; // ou resultado real
                long duration = System.currentTimeMillis() - startTime;

                if (status == 200) {
                    // INFO: Sucesso
                    StructuredLogger.setDuration(duration);
                    StructuredLogger.put("status_code", String.valueOf(status));
                    logger.info("External API call completed successfully");
                } else if (status >= 400 && status < 500) {
                    // WARN: Erro do cliente (não é problema do nosso sistema)
                    StructuredLogger.setDuration(duration);
                    StructuredLogger.put("status_code", String.valueOf(status));
                    logger.warn("External API returned client error");
                } else {
                    // ERROR: Erro do servidor externo
                    StructuredLogger.setError("EXTERNAL_API_ERROR", "Server returned " + status);
                    logger.error("External API call failed");
                }

            } catch (Exception e) {
                // ERROR: Exceção inesperada
                StructuredLogger.setError("EXTERNAL_API_EXCEPTION", e.getMessage());
                logger.error("Failed to call external API", e);
            } finally {
                StructuredLogger.clear();
            }
        }
    }

    /**
     * Exemplo 4: Log de performance (INFO + PERFORMANCE)
     */
    public static class PerformanceExample {
        private static final Logger logger = LoggerFactory.getLogger(PerformanceExample.class);

        public void trackSlowQuery(String query, long executionTime) {
            try {
                StructuredLogger.generateCorrelationId();
                StructuredLogger.setCategory(LogCategory.PERFORMANCE);
                StructuredLogger.setDuration(executionTime);
                StructuredLogger.put("query_type", "DATABASE");

                if (executionTime > 1000) {
                    // WARN: Query lenta (não é erro, mas precisa atenção)
                    logger.warn("Slow database query detected: {}ms", executionTime);
                } else {
                    // INFO: Query normal
                    logger.info("Database query executed: {}ms", executionTime);
                }

            } finally {
                StructuredLogger.clear();
            }
        }
    }
}
