package com.epic.cms.repository;

import com.epic.cms.service.CardEncryptionService;
import com.epic.cms.mapper.CardRequestRowMapper;
import com.epic.cms.model.CardRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CardRequestRepository {

    private static final Logger logger = LoggerFactory.getLogger(CardRequestRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final CardRequestRowMapper rowMapper;
    private final CardEncryptionService encryptionService;

    public CardRequestRepository(JdbcTemplate jdbcTemplate, CardRequestRowMapper rowMapper, CardEncryptionService encryptionService) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.encryptionService = encryptionService;
    }

    public void save(CardRequest cardRequest) {
        String sql = """
            INSERT INTO card_request 
            (card_number, request_reason_code, status_code, create_time)
            VALUES (?, ?, ?, ?)
        """;

        // Encrypt card number before saving to maintain foreign key integrity with 'card' table
        String encryptedCardNumber = isEncrypted(cardRequest.getCardNumber()) 
            ? cardRequest.getCardNumber() 
            : encryptionService.encrypt(cardRequest.getCardNumber());

        jdbcTemplate.update(sql,
                encryptedCardNumber,
                cardRequest.getRequestReasonCode(),
                cardRequest.getStatusCode(),
                java.sql.Timestamp.valueOf(cardRequest.getCreateTime())
        );
    }

    public Optional<CardRequest> findById(Long requestId) {
        String sql = "SELECT * FROM card_request WHERE request_id = ?";
        Optional<CardRequest> result = jdbcTemplate.query(sql, rowMapper, requestId)
                .stream()
                .findFirst();
        
        result.ifPresent(this::decryptCardNumber);
        return result;
    }

    public List<CardRequest> findAll() {
        String sql = "SELECT * FROM card_request ORDER BY create_time DESC";
        List<CardRequest> results = jdbcTemplate.query(sql, rowMapper);
        results.forEach(this::decryptCardNumber);
        return results;
    }

    public List<CardRequest> findAllWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM card_request ORDER BY create_time DESC LIMIT ? OFFSET ?";
        List<CardRequest> results = jdbcTemplate.query(sql, rowMapper, limit, offset);
        results.forEach(this::decryptCardNumber);
        return results;
    }

    public long countAllRequests() {
        String sql = "SELECT COUNT(*) FROM card_request";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public void update(CardRequest cardRequest) {
        String sql = """
            UPDATE card_request 
            SET status_code = ?
            WHERE request_id = ?
        """;

        jdbcTemplate.update(sql,
                cardRequest.getStatusCode(),
                cardRequest.getRequestId()
        );
    }

    public List<CardRequest> findPendingRequestsByCardNumber(String cardNumber) {
        // Encrypt plain text card number for DB lookup to match 'card' table foreign key
        String encryptedCardNumber = isEncrypted(cardNumber) ? cardNumber : encryptionService.encrypt(cardNumber);
        
        String sql = """
            SELECT * FROM card_request 
            WHERE card_number = ? AND status_code = 'PENDING'
            ORDER BY create_time DESC
        """;
        List<CardRequest> results = jdbcTemplate.query(sql, rowMapper, encryptedCardNumber);
        results.forEach(this::decryptCardNumber);
        return results;
    }

    private void decryptCardNumber(CardRequest request) {
        if (request.getCardNumber() != null && isEncrypted(request.getCardNumber())) {
            try {
                request.setCardNumber(encryptionService.decrypt(request.getCardNumber()));
            } catch (Exception e) {
                logger.error("Error decrypting card number in CardRequest: {}", request.getCardNumber(), e);
            }
        }
    }

    private boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.length() > 20 || text.matches(".*[a-zA-Z+/=].*");
    }
}
