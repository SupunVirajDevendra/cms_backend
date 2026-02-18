package com.epic.cms.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardResponseDto {
    private String cardNumber; // This will contain the masked card number
    private String maskId;
    private LocalDate expiryDate;
    private String statusCode;
    private BigDecimal creditLimit;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private LocalDateTime lastUpdateTime;
}
