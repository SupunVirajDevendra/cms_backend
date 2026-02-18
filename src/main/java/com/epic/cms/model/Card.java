package com.epic.cms.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class Card {
    private String cardNumber;
    private LocalDate expiryDate;
    private String statusCode;
    private BigDecimal creditLimit;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private LocalDateTime lastUpdateTime;
}
