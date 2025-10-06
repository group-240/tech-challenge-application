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
 * Filtro HTTP para adicionar correlation ID e contexto de logging
 * automaticamente em todas as requisições.
 */
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String USER_ID_HEADER = "X-User-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Pegar ou gerar correlation ID
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = StructuredLogger.generateCorrelationId();
            } else {
                StructuredLogger.setCorrelationId(correlationId);
            }

            // Adicionar correlation ID na resposta
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Pegar user ID se disponível
            String userId = httpRequest.getHeader(USER_ID_HEADER);
            if (userId != null && !userId.isEmpty()) {
                StructuredLogger.setUserId(userId);
            }

            // Log da requisição
            long startTime = System.currentTimeMillis();
            String method = httpRequest.getMethod();
            String uri = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();

            logger.info("Incoming request: {} {} {}",
                    method,
                    uri,
                    queryString != null ? "?" + queryString : "");

            // Processar a requisição
            chain.doFilter(request, response);

            // Log da resposta
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            logger.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                    method,
                    uri,
                    status,
                    duration);

        } catch (Exception e) {
            logger.error("Error processing request", e);
            throw e;
        } finally {
            // Limpar contexto após processar requisição
            StructuredLogger.clear();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("LoggingFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("LoggingFilter destroyed");
    }
}
