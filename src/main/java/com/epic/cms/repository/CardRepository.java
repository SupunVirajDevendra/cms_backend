package com.epic.cms.repository;

import com.epic.cms.service.CardEncryptionService;
import com.epic.cms.mapper.CardRowMapper;
import com.epic.cms.model.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CardRepository {

    private static final Logger logger = LoggerFactory.getLogger(CardRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final CardRowMapper rowMapper;
    private final CardEncryptionService encryptionService;

    public CardRepository(JdbcTemplate jdbcTemplate, CardRowMapper rowMapper, CardEncryptionService encryptionService) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.encryptionService = encryptionService;
        logger.info("CardRepository initialized");
    }

    public List<Card> findAll() {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("findAll() - Executing query: SELECT * FROM card");
        long startTime = System.currentTimeMillis();
        
        try {
            String sql = "SELECT * FROM card";
            List<Card> result = jdbcTemplate.query(sql, rowMapper);
            
            // Decrypt card numbers
            result.forEach(this::decryptCardNumber);

            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("findAll() - Query executed in {}ms, returned {} records", duration, result.size());
            logger.info("findAll() - Retrieved {} cards from database", result.size());
            return result;
        } catch (Exception e) {
            logger.error("findAll() - Database error: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    public List<Card> findAllWithPagination(int offset, int limit) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("findAllWithPagination(offset={}, limit={}) - Executing query", offset, limit);
        long startTime = System.currentTimeMillis();
        
        try {
            String sql = "SELECT * FROM card ORDER BY card_number LIMIT ? OFFSET ?";
            List<Card> result = jdbcTemplate.query(sql, rowMapper, limit, offset);

            // Decrypt card numbers
            result.forEach(this::decryptCardNumber);

            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("findAllWithPagination(offset={}, limit={}) - Query executed in {}ms, returned {} records", 
                        offset, limit, duration, result.size());
            logger.info("findAllWithPagination(offset={}, limit={}) - Retrieved {} cards", offset, limit, result.size());
            return result;
        } catch (Exception e) {
            logger.error("findAllWithPagination(offset={}, limit={}) - Database error: {}", offset, limit, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    public long countAllCards() {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("countAllCards() - Executing query: SELECT COUNT(*) FROM card");
        long startTime = System.currentTimeMillis();
        
        try {
            String sql = "SELECT COUNT(*) FROM card";
            Long result = jdbcTemplate.queryForObject(sql, Long.class);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("countAllCards() - Query executed in {}ms, count: {}", duration, result);
            logger.info("countAllCards() - Total cards in database: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("countAllCards() - Database error: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    public Optional<Card> findByCardNumber(String cardNumber) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        // Encrypt the plain text card number to search in the database
        String encryptedCardNumber = encryptionService.encrypt(cardNumber);

        logger.debug("findByCardNumber(cardNumber={}) - Executing query", cardNumber);
        long startTime = System.currentTimeMillis();
        
        try {
            String sql = "SELECT * FROM card WHERE card_number = ?";
            List<Card> cards = jdbcTemplate.query(sql, rowMapper, encryptedCardNumber);
            
            Optional<Card> result = cards.isEmpty() ? Optional.empty() : Optional.of(cards.get(0));
            
            // Decrypt the retrieved card number (or just set the original plain text)
            result.ifPresent(card -> card.setCardNumber(cardNumber));

            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("findByCardNumber(cardNumber={}) - Query executed in {}ms, found: {}", 
                        cardNumber, duration, result.isPresent());
            
            if (result.isPresent()) {
                logger.info("findByCardNumber(cardNumber={}) - Card found with status: {}", 
                           cardNumber, result.get().getStatusCode());
            } else {
                logger.info("findByCardNumber(cardNumber={}) - Card not found", cardNumber);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("findByCardNumber(cardNumber={}) - Database error: {}", cardNumber, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    public void save(Card card) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        // Ensure card number is encrypted before saving
        String encryptedCardNumber = isEncrypted(card.getCardNumber()) 
            ? card.getCardNumber() 
            : encryptionService.encrypt(card.getCardNumber());

        logger.debug("save(cardNumber={}) - Executing INSERT", card.getCardNumber());
        logger.debug("save(cardNumber={}) - Card data: expiry_date={}, status_code={}, credit_limit={}, cash_limit={}", 
                    card.getCardNumber(), card.getExpiryDate(), card.getStatusCode(), 
                    card.getCreditLimit(), card.getCashLimit());
        long startTime = System.currentTimeMillis();
        
        try {
            String sql = """
                INSERT INTO card
                (card_number, expiry_date, status_code,
                 credit_limit, cash_limit,
                 available_credit_limit, available_cash_limit,
                 last_update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            int rowsAffected = jdbcTemplate.update(sql,
                    encryptedCardNumber,
                    java.sql.Date.valueOf(card.getExpiryDate()),
                    card.getStatusCode(),
                    card.getCreditLimit(),
                    card.getCashLimit(),
                    card.getAvailableCreditLimit(),
                    card.getAvailableCashLimit(),
                    java.sql.Timestamp.valueOf(card.getLastUpdateTime())
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (rowsAffected == 1) {
                logger.info("save(cardNumber={}) - Card inserted successfully in {}ms", card.getCardNumber(), duration);
                logger.debug("save(cardNumber={}) - Insert complete: availableCreditLimit={}, availableCashLimit={}", 
                            card.getCardNumber(), card.getAvailableCreditLimit(), card.getAvailableCashLimit());
            } else {
                logger.warn("save(cardNumber={}) - Unexpected row count: {}", card.getCardNumber(), rowsAffected);
            }
        } catch (Exception e) {
            logger.error("save(cardNumber={}) - Database error during insert: {}", card.getCardNumber(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    public void update(Card card) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("update(cardNumber={}) - Executing UPDATE", card.getCardNumber());
        logger.debug("update(cardNumber={}) - Update data: expiry_date={}, status_code={}, credit_limit={}, cash_limit={}", 
                    card.getCardNumber(), card.getExpiryDate(), card.getStatusCode(), 
                    card.getCreditLimit(), card.getCashLimit());
        long startTime = System.currentTimeMillis();
        
        try {
            String sql = """
                UPDATE card 
                SET expiry_date = ?, 
                    status_code = ?,
                    credit_limit = ?, 
                    cash_limit = ?, 
                    available_credit_limit = ?, 
                    available_cash_limit = ?, 
                    last_update_time = ?
                WHERE card_number = ?
            """;

            // Encrypt card number for WHERE clause
            String encryptedCardNumber = encryptionService.encrypt(card.getCardNumber());

            int rowsAffected = jdbcTemplate.update(sql,
                    java.sql.Date.valueOf(card.getExpiryDate()),
                    card.getStatusCode(),
                    card.getCreditLimit(),
                    card.getCashLimit(),
                    card.getAvailableCreditLimit(),
                    card.getAvailableCashLimit(),
                    java.sql.Timestamp.valueOf(card.getLastUpdateTime()),
                    encryptedCardNumber
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (rowsAffected == 1) {
                logger.info("update(cardNumber={}) - Card updated successfully in {}ms", card.getCardNumber(), duration);
                logger.debug("update(cardNumber={}) - Update complete: availableCreditLimit={}, availableCashLimit={}", 
                            card.getCardNumber(), card.getAvailableCreditLimit(), card.getAvailableCashLimit());
            } else if (rowsAffected == 0) {
                logger.warn("update(cardNumber={}) - No rows affected - card may not exist", card.getCardNumber());
            } else {
                logger.warn("update(cardNumber={}) - Unexpected row count: {}", card.getCardNumber(), rowsAffected);
            }
        } catch (Exception e) {
            logger.error("update(cardNumber={}) - Database error during update: {}", card.getCardNumber(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private void decryptCardNumber(Card card) {
        if (card.getCardNumber() != null && isEncrypted(card.getCardNumber())) {
            try {
                card.setCardNumber(encryptionService.decrypt(card.getCardNumber()));
            } catch (Exception e) {
                logger.error("Error decrypting card number: {}", card.getCardNumber(), e);
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
