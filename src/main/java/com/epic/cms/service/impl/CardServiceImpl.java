package com.epic.cms.service.impl;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.mapper.DtoMapper;
import com.epic.cms.model.Card;
import com.epic.cms.repository.CardRepository;
import com.epic.cms.service.CardService;
import com.epic.cms.service.CardEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository repository;
    private final DtoMapper dtoMapper;
    private final CardEncryptionService encryptionService;
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);

    public CardServiceImpl(CardRepository repository, DtoMapper dtoMapper, CardEncryptionService encryptionService) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.encryptionService = encryptionService;
        logger.info("CardServiceImpl initialized");
    }

    @Override
    public List<CardResponseDto> getAllCards() {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("getAllCards() - Starting operation");
        long startTime = System.currentTimeMillis();
        
        try {
            List<Card> cards = repository.findAll();
            List<CardResponseDto> result = dtoMapper.toCardResponseDtoList(cards);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("getAllCards() - Retrieved {} cards in {}ms", cards.size(), duration);
            logger.info("getAllCards() - Successfully retrieved {} cards", result.size());
            return result;
        } catch (Exception e) {
            logger.error("getAllCards() - Error retrieving cards: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public PageResponse<CardResponseDto> getAllCards(int page, int size) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("getAllCards(page={}, size={}) - Starting operation", page, size);
        long startTime = System.currentTimeMillis();
        
        try {
            int offset = page * size;
            List<Card> cards = repository.findAllWithPagination(offset, size);
            long totalElements = repository.countAllCards();
            
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            PageResponse<CardResponseDto> result = PageResponse.<CardResponseDto>builder()
                    .content(dtoMapper.toCardResponseDtoList(cards))
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(page == 0)
                    .last(page >= totalPages - 1)
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("getAllCards(page={}, size={}) - Retrieved {} cards (page {}/{}, total {}) in {}ms", 
                        page, size, result.getContent().size(), page + 1, totalPages, totalElements, duration);
            logger.info("getAllCards(page={}, size={}) - Successfully retrieved paginated results: {} cards", 
                       page, size, result.getContent().size());
            return result;
        } catch (Exception e) {
            logger.error("getAllCards(page={}, size={}) - Error retrieving paginated cards: {}", 
                        page, size, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public CardResponseDto getByCardNumber(String cardNumber) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("getByCardNumber(cardNumber={}) - Starting operation", cardNumber);
        long startTime = System.currentTimeMillis();
        
        try {
            Card card = repository.findByCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNumber));
            
            CardResponseDto result = dtoMapper.toCardResponseDto(card);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("getByCardNumber(cardNumber={}) - Retrieved card {} (status: {}) in {}ms", 
                        cardNumber, card.getStatusCode(), duration);
            logger.info("getByCardNumber(cardNumber={}) - Successfully retrieved card", cardNumber);
            return result;
        } catch (ResourceNotFoundException e) {
            logger.warn("getByCardNumber(cardNumber={}) - Card not found: {}", cardNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getByCardNumber(cardNumber={}) - Error retrieving card: {}", cardNumber, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void createCard(CreateCardDto dto) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.info("createCard(cardNumber={}) - Creating new card", dto.getCardNumber());
        logger.debug("createCard(cardNumber={}) - Card details: expiryDate={}, creditLimit={}, cashLimit={}", 
                    dto.getCardNumber(), dto.getExpiryDate(), dto.getCreditLimit(), dto.getCashLimit());
        long startTime = System.currentTimeMillis();

        try {
            // Check if card already exists
            if (repository.findByCardNumber(dto.getCardNumber()).isPresent()) {
                logger.warn("createCard(cardNumber={}) - Card already exists", dto.getCardNumber());
                throw new IllegalArgumentException("Card with number " + dto.getCardNumber() + " already exists");
            }

            Card card = Card.builder()
                    .cardNumber(encryptionService.encrypt(dto.getCardNumber()))
                    .expiryDate(dto.getExpiryDate())
                    .statusCode("IACT")
                    .creditLimit(dto.getCreditLimit())
                    .cashLimit(dto.getCashLimit())
                    .availableCreditLimit(dto.getCreditLimit())
                    .availableCashLimit(dto.getCashLimit())
                    .lastUpdateTime(LocalDateTime.now())
                    .build();

            repository.save(card);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("createCard(cardNumber={}) - Card created successfully in {}ms", dto.getCardNumber(), duration);
            logger.debug("createCard(cardNumber={}) - Card details: status={}, availableCreditLimit={}, availableCashLimit={}", 
                        dto.getCardNumber(), card.getStatusCode(), card.getAvailableCreditLimit(), card.getAvailableCashLimit());
        } catch (IllegalArgumentException e) {
            logger.warn("createCard(cardNumber={}) - Validation failed: {}", dto.getCardNumber(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("createCard(cardNumber={}) - Error creating card: {}", dto.getCardNumber(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void updateCard(String cardNumber, UpdateCardDto dto) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.info("updateCard(cardNumber={}) - Updating card", cardNumber);
        logger.debug("updateCard(cardNumber={}) - Update details: expiryDate={}, creditLimit={}, cashLimit={}", 
                    cardNumber, dto.getExpiryDate(), dto.getCreditLimit(), dto.getCashLimit());
        long startTime = System.currentTimeMillis();

        try {
            Card existingCard = repository.findByCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNumber));

            logger.debug("updateCard(cardNumber={}) - Current card status: {}", cardNumber, existingCard.getStatusCode());
            
            // Store old values for audit
            String oldExpiryDate = existingCard.getExpiryDate().toString();
            String oldCreditLimit = existingCard.getCreditLimit().toString();
            String oldCashLimit = existingCard.getCashLimit().toString();

            existingCard.setExpiryDate(dto.getExpiryDate());
            existingCard.setCardNumber(cardNumber); // Keep plain for the object, repo handles encryption
            existingCard.setCreditLimit(dto.getCreditLimit());
            existingCard.setCashLimit(dto.getCashLimit());
            existingCard.setAvailableCreditLimit(dto.getCreditLimit());
            existingCard.setAvailableCashLimit(dto.getCashLimit());
            existingCard.setLastUpdateTime(LocalDateTime.now());

            repository.update(existingCard);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("updateCard(cardNumber={}) - Card updated successfully in {}ms", cardNumber, duration);
            logger.debug("updateCard(cardNumber={}) - Changes: expiryDate {}→{}, creditLimit {}→{}, cashLimit {}→{}", 
                        cardNumber, oldExpiryDate, dto.getExpiryDate(), 
                        oldCreditLimit, dto.getCreditLimit(), 
                        oldCashLimit, dto.getCashLimit());
        } catch (ResourceNotFoundException e) {
            logger.warn("updateCard(cardNumber={}) - Card not found for update: {}", cardNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("updateCard(cardNumber={}) - Error updating card: {}", cardNumber, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}

