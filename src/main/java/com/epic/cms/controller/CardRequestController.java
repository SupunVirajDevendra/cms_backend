package com.epic.cms.controller;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.dto.EncryptedRequest;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.service.CardRequestService;
import com.epic.cms.service.PayloadDecryptionService;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/card-requests")
@Tag(name = "Card Request Management", description = "APIs for managing card requests")
public class CardRequestController {

    private static final Logger logger = LoggerFactory.getLogger(CardRequestController.class);
    private final CardRequestService cardRequestService;
    private final PayloadDecryptionService decryptionService;

    public CardRequestController(CardRequestService cardRequestService, PayloadDecryptionService decryptionService) {
        this.cardRequestService = cardRequestService;
        this.decryptionService = decryptionService;
        logger.info("CardRequestController initialized");
    }

    @PostMapping
    @Operation(summary = "Create card request", description = "Create a new card request (activation or closure)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created card request"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> createRequest(@Valid @RequestBody EncryptedRequest encryptedRequest) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("POST /api/card-requests - Creating card request from encrypted payload");
        long startTime = System.currentTimeMillis();
        
        try {
            CreateCardRequestDto dto = decryptionService.decryptToObject(encryptedRequest.getPayload(), CreateCardRequestDto.class);
            
            logger.info("POST /api/card-requests - Decrypted request data: {} request for card: {}", 
                       dto.getRequestReasonCode(), dto.getCardIdentifier());
            
            cardRequestService.createRequest(dto);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("POST /api/card-requests - Successfully created {} request for card {} in {}ms", 
                       dto.getRequestReasonCode(), dto.getCardIdentifier(), duration);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            logger.error("POST /api/card-requests - Error creating card request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } finally {
            MDC.clear();
        }
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
            @Valid @RequestBody EncryptedRequest encryptedRequest) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("PUT /api/card-requests/{}/process - Processing request from encrypted payload", id);
        long startTime = System.currentTimeMillis();
        
        try {
            ActionDto action = decryptionService.decryptToObject(encryptedRequest.getPayload(), ActionDto.class);
            
            String actionStr = Boolean.TRUE.equals(action.getApprove()) ? "APPROVE" : "REJECT";
            logger.info("PUT /api/card-requests/{}/process - Decrypted action: {}", id, actionStr);
            
            cardRequestService.processRequest(id, action);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("PUT /api/card-requests/{}/process - Successfully processed request with action: {} in {}ms", 
                       id, actionStr, duration);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("PUT /api/card-requests/{}/process - Error processing request: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } finally {
            MDC.clear();
        }
    }

    @GetMapping
    @Operation(summary = "Get all card requests", description = "Retrieve a list of all card requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of requests")
    })
    public ResponseEntity<List<CardRequestResponseDto>> getAllRequests() {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("GET /api/card-requests - Retrieving all card requests");
        long startTime = System.currentTimeMillis();
        
        try {
            List<CardRequestResponseDto> requests = cardRequestService.getAllRequests();
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("GET /api/card-requests - Successfully retrieved {} requests in {}ms", 
                       requests.size(), duration);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("GET /api/card-requests - Error retrieving requests: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all card requests with pagination", description = "Retrieve a paginated list of card requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of requests")
    })
    public ResponseEntity<PageResponse<CardRequestResponseDto>> getAllRequestsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("GET /api/card-requests/paginated - Retrieving requests with page={}, size={}", page, size);
        long startTime = System.currentTimeMillis();
        
        try {
            PageResponse<CardRequestResponseDto> response = cardRequestService.getAllRequests(page, size);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("GET /api/card-requests/paginated - Successfully retrieved {} requests (page {}/{}, total {}) in {}ms", 
                       response.getContent().size(), response.getPageNumber() + 1, 
                       response.getTotalPages(), response.getTotalElements(), duration);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("GET /api/card-requests/paginated - Error retrieving paginated requests: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
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
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.info("GET /api/card-requests/{} - Retrieving request by ID", id);
        long startTime = System.currentTimeMillis();
        
        try {
            CardRequestResponseDto request = cardRequestService.getRequestById(id);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("GET /api/card-requests/{} - Successfully retrieved request {} (card: {}, type: {}) in {}ms", 
                       id, request.getRequestId(), request.getCardNumber(), request.getRequestReasonCode(), duration);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            logger.error("GET /api/card-requests/{} - Error retrieving request: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @Operation(summary = "Handle validation errors", description = "Returns validation error details")
    @ApiResponse(responseCode = "400", description = "Validation errors")
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.warn("Validation error in request: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.debug("Validation error - Field: {}, Error: {}", fieldName, errorMessage);
        });
        
        logger.warn("Returning {} validation errors", errors.size());
        MDC.clear();
        return ResponseEntity.badRequest().body(errors);
    }
}
