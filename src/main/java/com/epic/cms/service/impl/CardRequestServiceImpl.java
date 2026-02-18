package com.epic.cms.service.impl;

import com.epic.cms.dto.ActionDto;
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

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public void processRequest(Long requestId, ActionDto action) {
        // Validate request exists
        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        // Validate request status is PENDING
        if (!"PENDING".equals(request.getStatusCode())) {
            throw new BusinessException("Request is not PENDING: " + requestId);
        }

        if (Boolean.TRUE.equals(action.getApprove())) {
            // APPROVE logic
            // Validate card exists
            Card card = cardRepository.findByCardNumber(request.getCardNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getCardNumber()));

            // Business rules for approval
            if ("ACTI".equals(request.getRequestReasonCode())) {
                // ACTI: Card must be IACT (Inactive)
                if (!"IACT".equals(card.getStatusCode())) {
                    throw new BusinessException("Cannot activate card: Card must be INACTIVE (IACT)");
                }
                // Update card to CACT (Active)
                card.setStatusCode("CACT");
                cardRepository.update(card);
                
            } else if ("CDCL".equals(request.getRequestReasonCode())) {
                // CDCL: Card must be CACT (Active)
                if (!"CACT".equals(card.getStatusCode())) {
                    throw new BusinessException("Cannot close card: Card must be ACTIVE (CACT)");
                }
                // Update card to DACT (Deactivated)
                card.setStatusCode("DACT");
                cardRepository.update(card);
            }

            // Update request status to APPROVED
            request.setStatusCode("APPROVED");
            cardRequestRepository.update(request);

            logger.info("Request approved: {} for card: {} with type: {}", 
                       requestId, request.getCardNumber(), request.getRequestReasonCode());

        } else {
            // REJECT logic (card remains unchanged)
            request.setStatusCode("REJECTED");
            cardRequestRepository.update(request);

            logger.info("Request rejected: {} for card: {} with type: {}", 
                       requestId, request.getCardNumber(), request.getRequestReasonCode());
        }
    }

    @Override
    public List<CardRequest> getAllRequests() {
        return cardRequestRepository.findAll();
    }

    @Override
    public CardRequest getRequestById(Long requestId) {
        return cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
    }
}
