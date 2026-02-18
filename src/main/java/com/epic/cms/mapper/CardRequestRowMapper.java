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
        CardRequest cardRequest = new CardRequest();
        cardRequest.setRequestId(rs.getLong("request_id"));
        cardRequest.setCardNumber(rs.getString("card_number"));
        cardRequest.setRequestReasonCode(rs.getString("request_reason_code"));
        cardRequest.setStatusCode(rs.getString("status_code"));
        
        LocalDateTime createTime = rs.getTimestamp("create_time") != null 
            ? rs.getTimestamp("create_time").toLocalDateTime() 
            : null;
        cardRequest.setCreateTime(createTime);
        
        return cardRequest;
    }
}
