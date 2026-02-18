package com.epic.cms.repository;

import com.epic.cms.mapper.CardRequestRowMapper;
import com.epic.cms.model.CardRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
}
