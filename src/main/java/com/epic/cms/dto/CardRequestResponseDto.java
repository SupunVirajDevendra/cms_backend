package com.epic.cms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CardRequestResponseDto {
    private Long requestId;
    private String cardNumber; // This will contain the masked card number
    private String maskId;
    private String requestReasonCode;
    private String statusCode;
    private LocalDateTime createTime;
}
