package com.epic.cms.mapper;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.model.Card;
import com.epic.cms.model.CardRequest;
import com.epic.cms.util.CardNumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {
    
    public CardResponseDto toCardResponseDto(Card card) {
        if (card == null) {
            return null;
        }
        
        CardResponseDto dto = new CardResponseDto();
        dto.setCardNumber(CardNumberUtils.maskCardNumber(card.getCardNumber())); // Set masked number as cardNumber
        dto.setMaskId(CardNumberUtils.generateMaskId(card.getCardNumber()));
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
        dto.setCardNumber(CardNumberUtils.maskCardNumber(cardRequest.getCardNumber())); // Set masked number as cardNumber
        dto.setMaskId(CardNumberUtils.generateMaskId(cardRequest.getCardNumber()));
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
