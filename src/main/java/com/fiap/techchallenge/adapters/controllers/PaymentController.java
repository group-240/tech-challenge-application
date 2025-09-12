package com.fiap.techchallenge.adapters.controllers;

import com.fiap.techchallenge.application.usecases.PaymentUseCase;

public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    public Long createPaymentOrder(
        Double amount,
        String description,
        String paymentMethodId,
        Integer installments,
        String payerEmail,
        String identificationType,
        String identificationNumber
    ) {
        return paymentUseCase.createPaymentOrder(
            amount,
            description,
            paymentMethodId,
            installments,
            payerEmail,
            identificationType,
            identificationNumber
        );
    }
}
