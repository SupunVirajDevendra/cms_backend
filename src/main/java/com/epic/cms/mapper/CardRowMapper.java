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

        Card card = new Card();
        card.setCardNumber(rs.getString("card_number"));
        card.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        card.setStatusCode(rs.getString("status_code"));
        card.setCreditLimit(rs.getBigDecimal("credit_limit"));
        card.setCashLimit(rs.getBigDecimal("cash_limit"));
        card.setAvailableCreditLimit(rs.getBigDecimal("available_credit_limit"));
        card.setAvailableCashLimit(rs.getBigDecimal("available_cash_limit"));
        card.setLastUpdateTime(rs.getTimestamp("last_update_time").toLocalDateTime());

        return card;
    }
}