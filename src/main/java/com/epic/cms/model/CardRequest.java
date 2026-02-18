package com.epic.cms.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CardRequest {
    private Long requestId;
    private String cardNumber;
    private String requestReasonCode;   // ACTI / CDCL
    private String statusCode;          // PENDING / APPROVED / REJECTED
    private LocalDateTime createTime;
}

