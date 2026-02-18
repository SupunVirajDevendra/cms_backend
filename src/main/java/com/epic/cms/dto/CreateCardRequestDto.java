package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCardRequestDto {

    @NotBlank
    private String cardNumber;

    @NotBlank
    private String requestReasonCode;   // ACTI / CDCL

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getRequestReasonCode() {
        return requestReasonCode;
    }

    public void setRequestReasonCode(String requestReasonCode) {
        this.requestReasonCode = requestReasonCode;
    }
}
