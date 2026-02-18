package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCardDto {

    @NotBlank
    private String cardNumber;

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    private BigDecimal creditLimit;

    @NotNull
    private BigDecimal cashLimit;
}
