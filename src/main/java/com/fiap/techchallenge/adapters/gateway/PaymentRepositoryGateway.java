package com.fiap.techchallenge.adapters.gateway;

import com.fiap.techchallenge.domain.repositories.PaymentRepository;
import com.fiap.techchallenge.external.datasource.mercadopago.MercadoPagoClient;

public class PaymentRepositoryGateway implements PaymentRepository {

    private final MercadoPagoClient mercadoPagoClient;

    public PaymentRepositoryGateway(MercadoPagoClient mercadoPagoClient) {
        this.mercadoPagoClient = mercadoPagoClient;
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
        return mercadoPagoClient.createPaymentOrder(
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
