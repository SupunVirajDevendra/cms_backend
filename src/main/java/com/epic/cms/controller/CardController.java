package com.epic.cms.controller;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.EncryptedRequest;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.service.CardService;
import com.epic.cms.service.PayloadDecryptionService;
import com.epic.cms.util.CardNumberResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/cards")
@Tag(name = "Card Management", description = "APIs for managing credit cards")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);
    private final CardService service;
    private final CardNumberResolver cardNumberResolver;
    private final PayloadDecryptionService decryptionService;

    public CardController(CardService service, CardNumberResolver cardNumberResolver, PayloadDecryptionService decryptionService) {
        this.service = service;
        this.cardNumberResolver = cardNumberResolver;
        this.decryptionService = decryptionService;
        logger.info("CardController initialized");
    }

    @GetMapping
    @Operation(summary = "Get all cards", description = "Retrieve a list of all credit cards")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards")
    })
    public ResponseEntity<List<CardResponseDto>> getAll() {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("GET /api/cards - Retrieving all cards");
        long startTime = System.currentTimeMillis();
        
        try {
            List<CardResponseDto> cards = service.getAllCards();
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("GET /api/cards - Successfully retrieved {} cards in {}ms", cards.size(), duration);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            logger.error("GET /api/cards - Error retrieving cards: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all cards with pagination", description = "Retrieve a paginated list of credit cards")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of cards")
    })
    public ResponseEntity<PageResponse<CardResponseDto>> getAllPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("GET /api/cards/paginated - Retrieving cards with page={}, size={}", page, size);
        long startTime = System.currentTimeMillis();
        
        try {
            PageResponse<CardResponseDto> response = service.getAllCards(page, size);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("GET /api/cards/paginated - Successfully retrieved {} cards (page {}/{}, total {}) in {}ms", 
                       response.getContent().size(), response.getPageNumber() + 1, 
                       response.getTotalPages(), response.getTotalElements(), duration);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("GET /api/cards/paginated - Error retrieving paginated cards: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{cardIdentifier}")
    @Operation(summary = "Get card by identifier", description = "Retrieve a card by plain number, masked number, or mask ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved card"),
        @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardResponseDto> getByIdentifier(
            @Parameter(description = "Card identifier (plain number, masked number, or mask ID)") 
            @PathVariable String cardIdentifier) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("GET /api/cards/{} - Retrieving card by identifier", cardIdentifier);
        long startTime = System.currentTimeMillis();
        
        try {
            // Accept: plain card number, masked card number, or mask ID
            Optional<Card> card = cardNumberResolver.resolveCard(cardIdentifier);
            if (card.isEmpty()) {
                logger.warn("GET /api/cards/{} - Card not found", cardIdentifier);
                throw new ResourceNotFoundException("Card not found: " + cardIdentifier);
            }
            
            CardResponseDto response = service.getByCardNumber(card.get().getCardNumber());
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("GET /api/cards/{} - Successfully retrieved card {} in {}ms", 
                       cardIdentifier, card.get().getCardNumber(), duration);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("GET /api/cards/{} - Card not found: {}", cardIdentifier, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("GET /api/cards/{} - Error retrieving card: {}", cardIdentifier, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/{cardIdentifier}")
    @Operation(summary = "Update card", description = "Update card details by identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated card"),
        @ApiResponse(responseCode = "404", description = "Card not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void> update(
            @Parameter(description = "Card identifier (plain number, masked number, or mask ID)") 
            @PathVariable String cardIdentifier, 
            @Valid @RequestBody EncryptedRequest encryptedRequest) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("PUT /api/cards/{} - Updating card from encrypted payload", cardIdentifier);
        long startTime = System.currentTimeMillis();
        
        try {
            UpdateCardDto dto = decryptionService.decryptToObject(encryptedRequest.getPayload(), UpdateCardDto.class);
            
            logger.info("PUT /api/cards/{} - Decrypted update data: creditLimit={}, cashLimit={}, expiryDate={}", 
                       cardIdentifier, dto.getCreditLimit(), dto.getCashLimit(), dto.getExpiryDate());
            
            // Accept: plain card number, masked card number, or mask ID
            Optional<Card> card = cardNumberResolver.resolveCard(cardIdentifier);
            if (card.isEmpty()) {
                logger.warn("PUT /api/cards/{} - Card not found for update", cardIdentifier);
                throw new ResourceNotFoundException("Card not found: " + cardIdentifier);
            }
            
            service.updateCard(card.get().getCardNumber(), dto);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("PUT /api/cards/{} - Successfully updated card {} in {}ms", 
                       cardIdentifier, card.get().getCardNumber(), duration);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("PUT /api/cards/{} - Card not found for update: {}", cardIdentifier, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("PUT /api/cards/{} - Error updating card: {}", cardIdentifier, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } finally {
            MDC.clear();
        }
    }

    @PostMapping
    @Operation(summary = "Create new card", description = "Create a new credit card")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created card"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void> create(@Valid @RequestBody EncryptedRequest encryptedRequest) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("POST /api/cards - Creating new card from encrypted payload");
        long startTime = System.currentTimeMillis();
        
        try {
            CreateCardDto dto = decryptionService.decryptToObject(encryptedRequest.getPayload(), CreateCardDto.class);
            
            logger.info("POST /api/cards - Decrypted card data: creditLimit={}, cashLimit={}, expiryDate={", 
                       dto.getCreditLimit(), dto.getCashLimit(), dto.getExpiryDate());
            
            service.createCard(dto);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("POST /api/cards - Successfully created card {} in {}ms", dto.getCardNumber(), duration);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            logger.error("POST /api/cards - Error creating card: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } finally {
            MDC.clear();
        }
    }
}

