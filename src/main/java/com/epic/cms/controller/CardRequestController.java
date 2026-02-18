package com.epic.cms.controller;

import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.service.CardRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card-requests")
public class CardRequestController {

    private final CardRequestService cardRequestService;

    public CardRequestController(CardRequestService cardRequestService) {
        this.cardRequestService = cardRequestService;
    }

    @PostMapping
    public ResponseEntity<Void> createRequest(@Valid @RequestBody CreateCardRequestDto dto) {
        cardRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
