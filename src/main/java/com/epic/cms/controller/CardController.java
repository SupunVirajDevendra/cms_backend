package com.epic.cms.controller;

import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.model.Card;
import com.epic.cms.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Card>> getAll() {
        return ResponseEntity.ok(service.getAllCards());
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<Card> getByCardNumber(@PathVariable String cardNumber) {
        return ResponseEntity.ok(service.getByCardNumber(cardNumber));
    }



    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCardDto dto) {
        service.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{cardNumber}")
    public ResponseEntity<Void> update(@PathVariable String cardNumber, 
                                       @Valid @RequestBody UpdateCardDto dto) {
        service.updateCard(cardNumber, dto);
        return ResponseEntity.ok().build();
    }
}

