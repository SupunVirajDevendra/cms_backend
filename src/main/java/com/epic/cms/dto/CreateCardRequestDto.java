package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCardRequestDto {

    @NotBlank
    private String cardNumber;

    @NotBlank
    private String requestReasonCode;   // ACTI / CDCL
}
