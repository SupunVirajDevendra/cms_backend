package com.epic.cms.controller;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.model.CardRequest;
import com.epic.cms.service.CardRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{id}/process")
    public ResponseEntity<Void> processRequest(@PathVariable Long id, @Valid @RequestBody ActionDto action) {
        cardRequestService.processRequest(id, action);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CardRequest>> getAllRequests() {
        List<CardRequest> requests = cardRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardRequest> getRequestById(@PathVariable Long id) {
        CardRequest request = cardRequestService.getRequestById(id);
        return ResponseEntity.ok(request);
    }
}
