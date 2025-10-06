package com.fiap.techchallenge.infrastructure.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro HTTP seguindo práticas SRE para logging estruturado.
 * 
 * Responsabilidades:
 * - Adicionar correlation ID automaticamente
 * - Logar requisições e respostas HTTP
 * - Categorizar logs por severidade SRE
 * - Adicionar contexto para rastreabilidade
 * - Medir latência de requisições
 */
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String USER_ID_HEADER = "X-User-ID";
    
    // Threshold SRE para latência (500ms = aceitável, >500ms = lento)
    private static final long SLOW_REQUEST_THRESHOLD_MS = 500;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String fullPath = uri + (queryString != null ? "?" + queryString : "");

        try {
            // ================================================================
            // SETUP - Correlation ID e contexto
            // ================================================================
            
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = StructuredLogger.generateCorrelationId();
            } else {
                StructuredLogger.setCorrelationId(correlationId);
            }
            
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            // User ID (se disponível)
            String userId = httpRequest.getHeader(USER_ID_HEADER);
            if (userId != null && !userId.isEmpty()) {
                StructuredLogger.setUserId(userId);
            }

            // Contexto HTTP
            StructuredLogger.put("http_method", method);
            StructuredLogger.put("endpoint", uri);
            StructuredLogger.put("query_string", queryString != null ? queryString : "");
            StructuredLogger.put("remote_addr", httpRequest.getRemoteAddr());
            StructuredLogger.put("user_agent", httpRequest.getHeader("User-Agent"));

            // ================================================================
            // INFO: Requisição recebida
            // ================================================================
            logger.info("HTTP request received: {} {}", method, fullPath);

            // Processar requisição
            chain.doFilter(request, response);

            // ================================================================
            // LOG DE RESPOSTA - Categorização SRE por status code
            // ================================================================
            
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();
            
            StructuredLogger.setDuration(duration);
            StructuredLogger.put("http_status", String.valueOf(status));

            // Categorização SRE baseada no status HTTP
            if (status >= 200 && status < 300) {
                // INFO: Sucesso (2xx)
                if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                    // WARN: Sucesso mas lento
                    StructuredLogger.setCategory(LogCategory.PERFORMANCE);
                    logger.warn("HTTP request completed successfully but SLOW: {} {} - Status: {} - Duration: {}ms", 
                            method, fullPath, status, duration);
                } else {
                    // INFO: Sucesso normal
                    logger.info("HTTP request completed: {} {} - Status: {} - Duration: {}ms", 
                            method, fullPath, status, duration);
                }
                
            } else if (status >= 300 && status < 400) {
                // INFO: Redirecionamento (3xx) - comportamento normal
                logger.info("HTTP request redirected: {} {} - Status: {} - Duration: {}ms", 
                        method, fullPath, status, duration);
                
            } else if (status >= 400 && status < 500) {
                // WARN: Erro do cliente (4xx) - não é problema do servidor
                StructuredLogger.setError("CLIENT_ERROR", "HTTP " + status);
                logger.warn("HTTP request failed with client error: {} {} - Status: {} - Duration: {}ms", 
                        method, fullPath, status, duration);
                
            } else if (status >= 500) {
                // ERROR: Erro do servidor (5xx) - PROBLEMA NO SISTEMA
                StructuredLogger.setError("SERVER_ERROR", "HTTP " + status);
                logger.error("HTTP request failed with server error: {} {} - Status: {} - Duration: {}ms", 
                        method, fullPath, status, duration);
            }

        } catch (Exception e) {
            // ERROR: Exceção não tratada (CRÍTICO)
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            StructuredLogger.setError("UNHANDLED_EXCEPTION", e.getClass().getSimpleName());
            logger.error("Unhandled exception processing HTTP request: {} {} - Duration: {}ms", 
                    method, fullPath, duration, e);
            throw e;
            
        } finally {
            // Limpar contexto MDC
            StructuredLogger.clear();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // INFO: Inicialização de componente
        logger.info("LoggingFilter initialized with SRE logging strategy");
    }

    @Override
    public void destroy() {
        // INFO: Shutdown de componente
        logger.info("LoggingFilter destroyed");
    }
}
