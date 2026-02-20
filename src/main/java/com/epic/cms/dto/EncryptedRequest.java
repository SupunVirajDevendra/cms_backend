package com.epic.cms.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Encrypted request wrapper for frontend payload encryption")
public class EncryptedRequest {
    
    @Schema(description = "Base64 encrypted payload containing [IV + Ciphertext]", required = true)
    private String payload;
    
    public EncryptedRequest() {}
    
    public EncryptedRequest(String payload) {
        this.payload = payload;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
}
