package com.epic.cms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateCardDto {

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    private BigDecimal creditLimit;

    @NotNull
    private BigDecimal cashLimit;
}
