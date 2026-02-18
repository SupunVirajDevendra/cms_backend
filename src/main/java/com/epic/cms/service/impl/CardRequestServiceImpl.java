package com.epic.cms.service.impl;

import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.exception.BusinessException;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.model.CardRequest;
import com.epic.cms.repository.CardRepository;
import com.epic.cms.repository.CardRequestRepository;
import com.epic.cms.service.CardRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class CardRequestServiceImpl implements CardRequestService {

    private final CardRequestRepository cardRequestRepository;
    private final CardRepository cardRepository;
    private static final Logger logger = LoggerFactory.getLogger(CardRequestServiceImpl.class);

    public CardRequestServiceImpl(CardRequestRepository cardRequestRepository, 
                                CardRepository cardRepository) {
        this.cardRequestRepository = cardRequestRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public void createRequest(CreateCardRequestDto dto) {
        // Validate card exists
        Card card = cardRepository.findByCardNumber(dto.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + dto.getCardNumber()));

        // Business rules based on request type
        if ("CDCL".equals(dto.getRequestReasonCode())) {
            // For card close, check if available_credit_limit == credit_limit
            if (card.getAvailableCreditLimit().compareTo(card.getCreditLimit()) != 0) {
                throw new BusinessException("Cannot close card: Available credit limit must equal credit limit");
            }
        }

        // Create request with PENDING status
        CardRequest cardRequest = new CardRequest();
        cardRequest.setCardNumber(dto.getCardNumber());
        cardRequest.setRequestReasonCode(dto.getRequestReasonCode());
        cardRequest.setStatusCode("PENDING");
        cardRequest.setCreateTime(LocalDateTime.now());

        cardRequestRepository.save(cardRequest);

        logger.info("Card request created: {} for card: {} with type: {}", 
                   dto.getRequestReasonCode(), dto.getCardNumber(), dto.getRequestReasonCode());
    }
}
