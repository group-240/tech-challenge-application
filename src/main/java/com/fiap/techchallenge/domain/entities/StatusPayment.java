package com.fiap.techchallenge.domain.entities;

public enum StatusPayment {
    AGUARDANDO_PAGAMENTO,
    APROVADO,
    REJEITADO;

    // Método para converter status do Mercado Pago
    public static StatusPayment fromMercadoPagoStatus(String mpStatus) {
        switch (mpStatus.toLowerCase()) {
            case "approved":
                return APROVADO;
            case "pending":
            case "in_process":
                return AGUARDANDO_PAGAMENTO;
            case "rejected":
            case "cancelled":
                return REJEITADO;
            default:
                throw new IllegalArgumentException("Status desconhecido do Mercado Pago: " + mpStatus);
        }
    }
}
