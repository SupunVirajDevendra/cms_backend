package com.epic.cms.service.impl;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.exception.BusinessException;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.mapper.DtoMapper;
import com.epic.cms.model.Card;
import com.epic.cms.model.CardRequest;
import com.epic.cms.repository.CardRepository;
import com.epic.cms.repository.CardRequestRepository;
import com.epic.cms.service.CardEncryptionService;
import com.epic.cms.service.CardRequestService;
import com.epic.cms.util.CardNumberResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CardRequestServiceImpl implements CardRequestService {

    private final CardRequestRepository cardRequestRepository;
    private final CardRepository cardRepository;
    private final DtoMapper dtoMapper;
    private final CardNumberResolver cardNumberResolver;
    private final CardEncryptionService encryptionService;
    private static final Logger logger = LoggerFactory.getLogger(CardRequestServiceImpl.class);

    public CardRequestServiceImpl(CardRequestRepository cardRequestRepository, 
                                CardRepository cardRepository,
                                DtoMapper dtoMapper,
                                CardNumberResolver cardNumberResolver,
                                CardEncryptionService encryptionService) {
        this.cardRequestRepository = cardRequestRepository;
        this.cardRepository = cardRepository;
        this.dtoMapper = dtoMapper;
        this.cardNumberResolver = cardNumberResolver;
        this.encryptionService = encryptionService;
    }

    @Override
    public void createRequest(CreateCardRequestDto dto) {
        Optional<Card> cardOpt = cardNumberResolver.resolveCard(dto.getCardIdentifier());
        Card card = cardOpt.orElseThrow(() -> new ResourceNotFoundException("Card not found: " + dto.getCardIdentifier()));

        String encryptedCardNumber = encryptionService.encrypt(card.getCardNumber());
        
        logger.info("createRequest() - Resolved card: {}, original identifier: {}", card.getCardNumber(), dto.getCardIdentifier());

        List<CardRequest> existingPendingRequests = cardRequestRepository.findPendingRequestsByCardNumber(encryptedCardNumber);
        
        boolean hasSameTypePending = existingPendingRequests.stream()
                .anyMatch(req -> req.getRequestReasonCode().equals(dto.getRequestReasonCode()));
        
        if (hasSameTypePending) {
            throw new BusinessException("Cannot create request: There is already a pending " + 
                    ("ACTI".equals(dto.getRequestReasonCode()) ? "activation" : "closure") + 
                    " request for this card");
        }

        if ("CDCL".equals(dto.getRequestReasonCode())) {
            if (card.getAvailableCreditLimit().compareTo(card.getCreditLimit()) != 0) {
                throw new BusinessException("Cannot close card: Available credit limit must equal credit limit");
            }
        }

        CardRequest cardRequest = CardRequest.builder()
                .cardNumber(encryptedCardNumber)
                .requestReasonCode(dto.getRequestReasonCode())
                .statusCode("PENDING")
                .createTime(LocalDateTime.now())
                .build();

        cardRequestRepository.save(cardRequest);

        logger.info("Card request created: {} for card: {} with type: {}. Existing pending requests: {}", 
                   dto.getRequestReasonCode(), card.getCardNumber(), dto.getRequestReasonCode(), 
                   existingPendingRequests.size());
    }

    @Override
    public void processRequest(Long requestId, ActionDto action) {
        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        if (!"PENDING".equals(request.getStatusCode())) {
            throw new BusinessException("Request is not PENDING: " + requestId);
        }

        if (Boolean.TRUE.equals(action.getApprove())) {
            Card card = cardRepository.findByCardNumber(request.getCardNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getCardNumber()));

            if ("ACTI".equals(request.getRequestReasonCode())) {
                if (!"IACT".equals(card.getStatusCode())) {
                    throw new BusinessException("Cannot activate card: Card must be INACTIVE (IACT)");
                }
                card.setStatusCode("CACT");
                cardRepository.update(card);
                
            } else if ("CDCL".equals(request.getRequestReasonCode())) {
                if (!"CACT".equals(card.getStatusCode())) {
                    throw new BusinessException("Cannot close card: Card must be ACTIVE (CACT)");
                }
                card.setStatusCode("DACT");
                cardRepository.update(card);
            }

            request.setStatusCode("APPROVED");
            cardRequestRepository.update(request);

            logger.info("Request approved: {} for card: {} with type: {}", 
                       requestId, request.getCardNumber(), request.getRequestReasonCode());

        } else {
            request.setStatusCode("REJECTED");
            cardRequestRepository.update(request);

            logger.info("Request rejected: {} for card: {} with type: {}", 
                       requestId, request.getCardNumber(), request.getRequestReasonCode());
        }
    }

    @Override
    public List<CardRequestResponseDto> getAllRequests() {
        List<CardRequest> requests = cardRequestRepository.findAll();
        decryptCardRequestNumbers(requests);
        return dtoMapper.toCardRequestResponseDtoList(requests);
    }

    @Override
    public PageResponse<CardRequestResponseDto> getAllRequests(int page, int size) {
        int offset = page * size;
        List<CardRequest> requests = cardRequestRepository.findAllWithPagination(offset, size);
        decryptCardRequestNumbers(requests);
        long totalElements = cardRequestRepository.countAllRequests();
        
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PageResponse.<CardRequestResponseDto>builder()
                .content(dtoMapper.toCardRequestResponseDtoList(requests))
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    @Override
    public CardRequestResponseDto getRequestById(Long requestId) {
        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
        
        request.setCardNumber(encryptionService.decrypt(request.getCardNumber()));
        return dtoMapper.toCardRequestResponseDto(request);
    }

    private void decryptCardRequestNumbers(List<CardRequest> requests) {
        for (CardRequest request : requests) {
            try {
                request.setCardNumber(encryptionService.decrypt(request.getCardNumber()));
            } catch (Exception e) {
                logger.error("Error decrypting card request number: {}", request.getCardNumber(), e);
            }
        }
    }
}
