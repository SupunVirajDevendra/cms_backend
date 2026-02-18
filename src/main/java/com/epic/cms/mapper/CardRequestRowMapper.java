package com.epic.cms.mapper;

import com.epic.cms.model.CardRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class CardRequestRowMapper implements RowMapper<CardRequest> {

    @Override
    public CardRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardRequest cardRequest = CardRequest.builder()
                .requestId(rs.getLong("request_id"))
                .cardNumber(rs.getString("card_number"))
                .requestReasonCode(rs.getString("request_reason_code"))
                .statusCode(rs.getString("status_code"))
                .createTime(rs.getTimestamp("create_time") != null 
                    ? rs.getTimestamp("create_time").toLocalDateTime() 
                    : null)
                .build();
        
        return cardRequest;
    }
}
