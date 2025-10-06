package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.StatusPayment;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentNotificationUseCaseImpl implements PaymentNotificationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationUseCaseImpl.class);
    
    private final OrderUseCase orderUseCase;

    public PaymentNotificationUseCaseImpl(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @Override
    public void handlePaymentNotification(Long paymentId) {
        try {
            StructuredLogger.setCategory(LogCategory.INTEGRATION);
            StructuredLogger.setOperation("HandlePaymentNotification");
            StructuredLogger.setPaymentId(paymentId.toString());
            
            logger.info("Payment notification received: paymentId={}", paymentId);
            
            // Se o pedido existe, atualiza o status de pagamento para APROVADO
            orderUseCase.updateOrderStatusPayment(paymentId, StatusPayment.APROVADO);
            
            logger.info("Payment notification processed successfully: paymentId={}", paymentId);

        } catch (Exception e) {
            StructuredLogger.setError("PAYMENT_NOTIFICATION_FAILED", e.getMessage());
            logger.error("Failed to process payment notification: paymentId={}", paymentId, e);
        } finally {
            StructuredLogger.clear();
        }
    }
}
