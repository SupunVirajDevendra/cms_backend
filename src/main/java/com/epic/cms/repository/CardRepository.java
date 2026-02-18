package com.epic.cms.repository;

import com.epic.cms.mapper.CardRowMapper;
import com.epic.cms.model.Card;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CardRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CardRowMapper rowMapper;

    public CardRepository(JdbcTemplate jdbcTemplate,
                          CardRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public List<Card> findAll() {
        String sql = "SELECT * FROM card";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void save(Card card) {

        String sql = """
            INSERT INTO card
            (card_number, expiry_date, status_code,
             credit_limit, cash_limit,
             available_credit_limit, available_cash_limit,
             last_update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                card.getCardNumber(),
                card.getExpiryDate(),
                card.getStatusCode(),
                card.getCreditLimit(),
                card.getCashLimit(),
                card.getAvailableCreditLimit(),
                card.getAvailableCashLimit(),
                card.getLastUpdateTime()
        );
    }
}
