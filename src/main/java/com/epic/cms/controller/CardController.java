package com.epic.cms.controller;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.service.CardService;
import com.epic.cms.util.CardNumberResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService service;
    private final CardNumberResolver cardNumberResolver;

    public CardController(CardService service, CardNumberResolver cardNumberResolver) {
        this.service = service;
        this.cardNumberResolver = cardNumberResolver;
    }

    @GetMapping
    public ResponseEntity<List<CardResponseDto>> getAll() {
        return ResponseEntity.ok(service.getAllCards());
    }

    @GetMapping("/{cardIdentifier}")
    public ResponseEntity<CardResponseDto> getByIdentifier(@PathVariable String cardIdentifier) {
        // Accept: plain card number, masked card number, or mask ID
        Optional<Card> card = cardNumberResolver.resolveCard(cardIdentifier);
        if (card.isEmpty()) {
            throw new ResourceNotFoundException("Card not found: " + cardIdentifier);
        }
        
        CardResponseDto response = service.getByCardNumber(card.get().getCardNumber());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cardIdentifier}")
    public ResponseEntity<Void> update(@PathVariable String cardIdentifier, 
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
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCardDto dto) {
        service.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

