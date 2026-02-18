package com.epic.cms.controller;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.service.CardRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Card Request Management", description = "APIs for managing card requests")
public class CardRequestController {

    private final CardRequestService cardRequestService;

    public CardRequestController(CardRequestService cardRequestService) {
        this.cardRequestService = cardRequestService;
    }

    @PostMapping
    @Operation(summary = "Create card request", description = "Create a new card request (activation or closure)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created card request"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> createRequest(@Valid @RequestBody CreateCardRequestDto dto) {
        cardRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/process")
    @Operation(summary = "Process card request", description = "Approve or reject a card request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully processed request"),
        @ApiResponse(responseCode = "400", description = "Invalid action or request not in PENDING status"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<Void> processRequest(
            @Parameter(description = "Request ID") 
            @PathVariable Long id, 
            @Valid @RequestBody ActionDto action) {
        cardRequestService.processRequest(id, action);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all card requests", description = "Retrieve a list of all card requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of requests")
    })
    public ResponseEntity<List<CardRequestResponseDto>> getAllRequests() {
        List<CardRequestResponseDto> requests = cardRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all card requests with pagination", description = "Retrieve a paginated list of card requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of requests")
    })
    public ResponseEntity<PageResponse<CardRequestResponseDto>> getAllRequestsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardRequestService.getAllRequests(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card request by ID", description = "Retrieve a specific card request by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved request"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<CardRequestResponseDto> getRequestById(
            @Parameter(description = "Request ID") 
            @PathVariable Long id) {
        CardRequestResponseDto request = cardRequestService.getRequestById(id);
        return ResponseEntity.ok(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @Operation(summary = "Handle validation errors", description = "Returns validation error details")
    @ApiResponse(responseCode = "400", description = "Validation errors")
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
