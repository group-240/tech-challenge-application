package com.fiap.techchallenge.application.usecases;

public interface PaymentUseCase {
    Long createPaymentOrder(
        Double amount,
        String description,
        String paymentMethodId,
        Integer installments,
        String payerEmail,
        String identificationType,
        String identificationNumber
    );
}
