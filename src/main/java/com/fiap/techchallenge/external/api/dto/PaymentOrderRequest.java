package com.fiap.techchallenge.external.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class PaymentOrderRequest {

    @NotNull
    @DecimalMin("0.01")
    @Schema(example = "1.00", description = "Valor da ordem de pagamento")
    private Double amount;

    @NotBlank
    @Schema(example = "Test payment", description = "Descrição do pagamento")
    private String description;

    @NotBlank
    @Schema(example = "pix", description = "Método de pagamento")
    private String paymentMethodId;

    @Min(1)
    @Schema(example = "1", description = "Número de parcelas")
    private Integer installments;

    @Email
    @NotBlank
    @Schema(example = "teste@gmail.com", description = "Email do pagador")
    private String payerEmail;

    @NotBlank
    @Schema(example = "CPF", description = "Tipo de identificação (CPF ou CNPJ)")
    private String identificationType;

    @Schema(example = "99515210020", description = "Número de identificação (CPF/CNPJ)")
    private String identificationNumber;

    // Constructors
    public PaymentOrderRequest() {}

    // Getters and Setters
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public Integer getInstallments() { return installments; }
    public void setInstallments(Integer installments) { this.installments = installments; }

    public String getPayerEmail() { return payerEmail; }
    public void setPayerEmail(String payerEmail) { this.payerEmail = payerEmail; }

    public String getIdentificationType() { return identificationType; }
    public void setIdentificationType(String identificationType) { this.identificationType = identificationType; }

    public String getIdentificationNumber() { return identificationNumber; }
    public void setIdentificationNumber(String identificationNumber) { this.identificationNumber = identificationNumber; }
}
