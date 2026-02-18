package com.epic.cms.mapper;

import com.epic.cms.model.Card;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CardRowMapper implements RowMapper<Card> {

    @Override
    public Card mapRow(ResultSet rs, int rowNum) throws SQLException {

        Card card = Card.builder()
                .cardNumber(rs.getString("card_number"))
                .expiryDate(rs.getDate("expiry_date").toLocalDate())
                .statusCode(rs.getString("status_code"))
                .creditLimit(rs.getBigDecimal("credit_limit"))
                .cashLimit(rs.getBigDecimal("cash_limit"))
                .availableCreditLimit(rs.getBigDecimal("available_credit_limit"))
                .availableCashLimit(rs.getBigDecimal("available_cash_limit"))
                .lastUpdateTime(rs.getTimestamp("last_update_time").toLocalDateTime())
                .build();
                                                                
        return card;
    }
}