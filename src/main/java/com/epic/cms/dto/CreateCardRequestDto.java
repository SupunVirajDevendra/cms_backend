package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateCardRequestDto {

    @NotBlank(message = "Card identifier is required")
    private String cardIdentifier; // Accept: plain card number, masked card number, or mask ID

    @NotBlank(message = "Request reason code is required")
    @Pattern(regexp = "^(ACTI|CDCL)$", message = "Request reason code must be ACTI or CDCL")
    private String requestReasonCode;   // ACTI / CDCL
}
