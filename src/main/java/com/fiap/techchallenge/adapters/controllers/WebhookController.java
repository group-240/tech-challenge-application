package com.fiap.techchallenge.adapters.controllers;

import com.fiap.techchallenge.application.usecases.PaymentNotificationUseCase;

public class WebhookController {

    private final PaymentNotificationUseCase paymentNotificationUseCase;

    public WebhookController(PaymentNotificationUseCase paymentNotificationUseCase) {
        this.paymentNotificationUseCase = paymentNotificationUseCase;
    }

    public void handlePaymentNotification(Long paymentId) {
        if (paymentId != null) {
            paymentNotificationUseCase.handlePaymentNotification(paymentId);
        }
    }
}
