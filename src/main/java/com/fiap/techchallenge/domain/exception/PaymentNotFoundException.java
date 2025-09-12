package com.fiap.techchallenge.domain.exception;

public class PaymentNotFoundException  extends RuntimeException{
    public PaymentNotFoundException(String message) {
        super(message);
    }

}
