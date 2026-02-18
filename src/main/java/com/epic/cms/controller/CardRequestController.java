package com.epic.cms.controller;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.service.CardRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<CardRequestResponseDto>> getAllRequests() {
        List<CardRequestResponseDto> requests = cardRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardRequestResponseDto> getRequestById(@PathVariable Long id) {
        CardRequestResponseDto request = cardRequestService.getRequestById(id);
        return ResponseEntity.ok(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
