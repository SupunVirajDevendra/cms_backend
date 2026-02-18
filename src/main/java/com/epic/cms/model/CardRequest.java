package com.epic.cms.model;

import java.time.LocalDateTime;

public class CardRequest {

    private Long requestId;
    private String cardNumber;
    private String requestReasonCode;   // ACTI / CDCL
    private String statusCode;          // PENDING / APPROVED / REJECTED
    private LocalDateTime createTime;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getRequestReasonCode() { return requestReasonCode; }
    public void setRequestReasonCode(String requestReasonCode) { this.requestReasonCode = requestReasonCode; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

