package com.epic.cms.mapper;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.model.Card;
import com.epic.cms.model.CardRequest;
import com.epic.cms.util.CardNumberUtils;
import com.epic.cms.service.CardEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(DtoMapper.class);
    
    private final CardEncryptionService encryptionService;

    public DtoMapper(CardEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
    
    private boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // Card numbers are typically 16-19 digits. 
        // Encrypted strings (Base64) often contain characters like '+', '/', '=' and are usually longer.
        // Also check if it contains any non-digit characters (other than potential masking which shouldn't be here yet)
        return text.length() > 20 || text.matches(".*[a-zA-Z+/=].*");
    }

    public CardResponseDto toCardResponseDto(Card card) {
        if (card == null) {
            return null;
        }
        
        CardResponseDto dto = new CardResponseDto();
        String plainCardNumber = card.getCardNumber();
        
        // Robust check for encrypted or masked data
        if (isEncrypted(plainCardNumber)) {
            try {
                plainCardNumber = encryptionService.decrypt(plainCardNumber);
            } catch (Exception e) {
                logger.error("Failed to decrypt card number for masking: {}", plainCardNumber, e);
            }
        }
        
        // If it's still encrypted/Base64 after decryption attempt, we shouldn't mask it
        // This prevents "Qh4Zfk**********************************er4="
        String maskedNumber;
        if (isEncrypted(plainCardNumber)) {
            maskedNumber = plainCardNumber;
        } else if (plainCardNumber != null && plainCardNumber.contains("*")) {
            maskedNumber = plainCardNumber;
        } else {
            maskedNumber = CardNumberUtils.maskCardNumber(plainCardNumber);
        }
        
        dto.setCardNumber(maskedNumber); 
        dto.setMaskId(CardNumberUtils.generateMaskId(plainCardNumber));
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatusCode(card.getStatusCode());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setCashLimit(card.getCashLimit());
        dto.setAvailableCreditLimit(card.getAvailableCreditLimit());
        dto.setAvailableCashLimit(card.getAvailableCashLimit());
        dto.setLastUpdateTime(card.getLastUpdateTime());
        
        return dto;
    }
    
    public List<CardResponseDto> toCardResponseDtoList(List<Card> cards) {
        return cards.stream()
                .map(this::toCardResponseDto)
                .collect(Collectors.toList());
    }
    
    public CardRequestResponseDto toCardRequestResponseDto(CardRequest cardRequest) {
        if (cardRequest == null) {
            return null;
        }
        
        CardRequestResponseDto dto = new CardRequestResponseDto();
        dto.setRequestId(cardRequest.getRequestId());
        
        String plainCardNumber = cardRequest.getCardNumber();
        if (isEncrypted(plainCardNumber)) {
            try {
                plainCardNumber = encryptionService.decrypt(plainCardNumber);
            } catch (Exception e) {
                logger.error("Failed to decrypt card request number for masking: {}", plainCardNumber, e);
            }
        }

        // If it's still encrypted/Base64 after decryption attempt, we shouldn't mask it
        String maskedNumber;
        if (isEncrypted(plainCardNumber)) {
            maskedNumber = plainCardNumber;
        } else if (plainCardNumber != null && plainCardNumber.contains("*")) {
            maskedNumber = plainCardNumber;
        } else {
            maskedNumber = CardNumberUtils.maskCardNumber(plainCardNumber);
        }

        dto.setCardNumber(maskedNumber); 
        dto.setMaskId(CardNumberUtils.generateMaskId(plainCardNumber));
        dto.setRequestReasonCode(cardRequest.getRequestReasonCode());
        dto.setStatusCode(cardRequest.getStatusCode());
        dto.setCreateTime(cardRequest.getCreateTime());
        
        return dto;
    }
    
    public List<CardRequestResponseDto> toCardRequestResponseDtoList(List<CardRequest> cardRequests) {
        return cardRequests.stream()
                .map(this::toCardRequestResponseDto)
                .collect(Collectors.toList());
    }
}
