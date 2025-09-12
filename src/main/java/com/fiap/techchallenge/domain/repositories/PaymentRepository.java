package com.fiap.techchallenge.domain.repositories;

public interface PaymentRepository {
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
