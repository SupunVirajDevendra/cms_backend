package com.epic.cms.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateCardDto {

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    private BigDecimal creditLimit;

    @NotNull
    private BigDecimal cashLimit;

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCashLimit() {
        return cashLimit;
    }

    public void setCashLimit(BigDecimal cashLimit) {
        this.cashLimit = cashLimit;
    }
}
