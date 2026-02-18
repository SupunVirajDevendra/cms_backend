package com.epic.cms.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Card {
    private String cardNumber;
    private LocalDate expiryDate;
    private String statusCode;
    private BigDecimal creditLimit;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private LocalDateTime lastUpdateTime;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
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

    public BigDecimal getAvailableCreditLimit() {
        return availableCreditLimit;
    }

    public void setAvailableCreditLimit(BigDecimal availableCreditLimit) {
        this.availableCreditLimit = availableCreditLimit;
    }

    public BigDecimal getAvailableCashLimit() {
        return availableCashLimit;
    }

    public void setAvailableCashLimit(BigDecimal availableCashLimit) {
        this.availableCashLimit = availableCashLimit;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
