package com.epic.cms.repository;

import com.epic.cms.mapper.CardRequestRowMapper;
import com.epic.cms.model.CardRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CardRequestRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CardRequestRowMapper rowMapper;

    public CardRequestRepository(JdbcTemplate jdbcTemplate, CardRequestRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public void save(CardRequest cardRequest) {
        String sql = """
            INSERT INTO card_request 
            (card_number, request_reason_code, status_code, create_time)
            VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                cardRequest.getCardNumber(),
                cardRequest.getRequestReasonCode(),
                cardRequest.getStatusCode(),
                cardRequest.getCreateTime()
        );
    }

    public Optional<CardRequest> findById(Long requestId) {
        String sql = "SELECT * FROM card_request WHERE request_id = ?";
        return jdbcTemplate.query(sql, rowMapper, requestId)
                .stream()
                .findFirst();
    }

    public List<CardRequest> findAll() {
        String sql = "SELECT * FROM card_request ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<CardRequest> findAllWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM card_request ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, rowMapper, limit, offset);
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
}
