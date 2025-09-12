package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.StatusPayment;

public class PaymentNotificationUseCaseImpl implements PaymentNotificationUseCase {

    private final OrderUseCase orderUseCase;

    public PaymentNotificationUseCaseImpl(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @Override
    public void handlePaymentNotification(Long paymentId) {
        try {
            // Se o pedido existe, atualiza o status de pagamento para APROVADO
            orderUseCase.updateOrderStatusPayment(paymentId, StatusPayment.APROVADO);

        } catch (Exception e) {
            // do nothing
        }
    }
}
