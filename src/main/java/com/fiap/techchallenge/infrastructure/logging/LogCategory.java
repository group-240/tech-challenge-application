package com.fiap.techchallenge.infrastructure.logging;

/**
 * Categorização de logs seguindo práticas do Google SRE.
 * 
 * Referência: Site Reliability Engineering (Google)
 * https://sre.google/sre-book/monitoring-distributed-systems/
 */
public enum LogCategory {
    
    // ========================================================================
    // NÍVEIS DE SEVERIDADE SRE
    // ========================================================================
    
    /**
     * DEBUG: Informações detalhadas para diagnóstico durante desenvolvimento.
     * 
     * Quando usar:
     * - Valores de variáveis importantes
     * - Fluxo detalhado de execução
     * - Dados de entrada/saída de métodos
     * - Estados intermediários de processamento
     * 
     * NÃO usar em produção (muito verboso).
     */
    DEBUG,
    
    /**
     * INFO: Eventos normais e esperados do sistema.
     * 
     * Quando usar:
     * - Inicialização de serviços
     * - Operações bem-sucedidas (criação, atualização, exclusão)
     * - Marcos importantes do fluxo de negócio
     * - Conclusão de processamentos
     * - Mudanças de estado relevantes
     * 
     * Exemplos:
     * - "Order created successfully: orderId=123"
     * - "Payment processed: paymentId=456, amount=100.00"
     * - "Customer registered: customerId=789"
     */
    INFO,
    
    /**
     * WARN: Situações anormais que NÃO impedem a operação.
     * 
     * Quando usar:
     * - Situações recuperáveis
     * - Configurações faltando (usando defaults)
     * - Retries necessários (mas bem-sucedidos)
     * - Recursos próximos do limite
     * - Validações de negócio que falharam (esperado)
     * - APIs externas lentas (mas respondendo)
     * - Cache miss (degradação de performance)
     * 
     * Exemplos:
     * - "Product not found in cache, fetching from database: productId=123"
     * - "Retry attempt 2/3 for external API call"
     * - "Customer CPF already exists, returning existing customer"
     * - "Database connection pool 80% utilized"
     * 
     * ⚠️ WARN NÃO é erro! Sistema continua funcionando.
     */
    WARN,
    
    /**
     * ERROR: Erros que impedem uma operação específica.
     * 
     * Quando usar:
     * - Exceções inesperadas
     * - Falhas de integração com APIs externas
     * - Erros de banco de dados
     * - Timeouts
     * - Dados inválidos que causam falha
     * - Falha de autenticação/autorização
     * 
     * Exemplos:
     * - "Failed to save order to database: SQLException"
     * - "Payment gateway timeout after 30s"
     * - "Failed to authenticate with Cognito: InvalidTokenException"
     * - "Unable to connect to RDS instance"
     * 
     * ⚠️ Operação falhou, mas sistema continua rodando.
     * ⚠️ SEMPRE incluir exceção completa (stack trace).
     */
    ERROR,
    
    /**
     * FATAL: Erros críticos que impedem o funcionamento do sistema.
     * 
     * Quando usar:
     * - Falha ao iniciar a aplicação
     * - Perda de conexão com recursos críticos (BD, cache)
     * - Erros de configuração críticos
     * - OutOfMemoryError
     * - Corrupção de dados
     * 
     * Exemplos:
     * - "Failed to connect to database on startup"
     * - "OutOfMemoryError: Java heap space"
     * - "Configuration error: Missing required environment variable DB_HOST"
     * 
     * ⚠️ Sistema pode estar INDISPONÍVEL.
     * ⚠️ Requer intervenção IMEDIATA.
     */
    FATAL;
    
    // ========================================================================
    // CATEGORIAS ESPECIAIS SRE
    // ========================================================================
    
    /**
     * Categoria: AUDIT
     * Para logs de auditoria (quem fez o quê, quando).
     * Usar como MDC: StructuredLogger.setCategory("AUDIT")
     */
    public static final String AUDIT = "AUDIT";
    
    /**
     * Categoria: SECURITY
     * Para eventos de segurança (tentativas de acesso, falhas de auth).
     * Usar como MDC: StructuredLogger.setCategory("SECURITY")
     */
    public static final String SECURITY = "SECURITY";
    
    /**
     * Categoria: BUSINESS
     * Para eventos de negócio importantes (pedido criado, pagamento).
     * Usar como MDC: StructuredLogger.setCategory("BUSINESS")
     */
    public static final String BUSINESS = "BUSINESS";
    
    /**
     * Categoria: PERFORMANCE
     * Para métricas de performance (tempo de resposta, latência).
     * Usar como MDC: StructuredLogger.setCategory("PERFORMANCE")
     */
    public static final String PERFORMANCE = "PERFORMANCE";
    
    /**
     * Categoria: INTEGRATION
     * Para comunicação com sistemas externos (APIs, webhooks).
     * Usar como MDC: StructuredLogger.setCategory("INTEGRATION")
     */
    public static final String INTEGRATION = "INTEGRATION";
}
