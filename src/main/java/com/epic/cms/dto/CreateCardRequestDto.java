package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCardRequestDto {

    @NotBlank
    private String cardIdentifier; // Accept: plain card number, masked card number, or mask ID

    @NotBlank
    private String requestReasonCode;   // ACTI / CDCL
}
