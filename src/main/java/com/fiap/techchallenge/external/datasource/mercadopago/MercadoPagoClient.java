package com.fiap.techchallenge.external.datasource.mercadopago;

public interface MercadoPagoClient {
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
