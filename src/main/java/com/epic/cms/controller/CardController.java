package com.epic.cms.controller;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.service.CardService;
import com.epic.cms.util.CardNumberResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Card Management", description = "APIs for managing credit cards")
public class CardController {

    private final CardService service;
    private final CardNumberResolver cardNumberResolver;

    public CardController(CardService service, CardNumberResolver cardNumberResolver) {
        this.service = service;
        this.cardNumberResolver = cardNumberResolver;
    }

    @GetMapping
    @Operation(summary = "Get all cards", description = "Retrieve a list of all credit cards")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards")
    })
    public ResponseEntity<List<CardResponseDto>> getAll() {
        return ResponseEntity.ok(service.getAllCards());
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all cards with pagination", description = "Retrieve a paginated list of credit cards")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of cards")
    })
    public ResponseEntity<PageResponse<CardResponseDto>> getAllPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAllCards(page, size));
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
        // Accept: plain card number, masked card number, or mask ID
        Optional<Card> card = cardNumberResolver.resolveCard(cardIdentifier);
        if (card.isEmpty()) {
            throw new ResourceNotFoundException("Card not found: " + cardIdentifier);
        }
        
        CardResponseDto response = service.getByCardNumber(card.get().getCardNumber());
        return ResponseEntity.ok(response);
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
            @Valid @RequestBody UpdateCardDto dto) {
        // Accept: plain card number, masked card number, or mask ID
        Optional<Card> card = cardNumberResolver.resolveCard(cardIdentifier);
        if (card.isEmpty()) {
            throw new ResourceNotFoundException("Card not found: " + cardIdentifier);
        }
        
        service.updateCard(card.get().getCardNumber(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Operation(summary = "Create new card", description = "Create a new credit card")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created card"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCardDto dto) {
        service.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

