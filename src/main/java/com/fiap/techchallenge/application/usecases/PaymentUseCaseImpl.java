package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.repositories.PaymentRepository;

public class PaymentUseCaseImpl implements PaymentUseCase {

    private final PaymentRepository paymentRepository;

    public PaymentUseCaseImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Long createPaymentOrder(
        Double amount,
        String description,
        String paymentMethodId,
        Integer installments,
        String payerEmail,
        String identificationType,
        String identificationNumber
    ) {
        return paymentRepository.createPaymentOrder(
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
