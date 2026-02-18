package com.epic.cms.service;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.model.CardRequest;

import java.util.List;

public interface CardRequestService {

    void createRequest(CreateCardRequestDto dto);

    void processRequest(Long requestId, ActionDto action);

    List<CardRequest> getAllRequests();

    CardRequest getRequestById(Long requestId);
}
