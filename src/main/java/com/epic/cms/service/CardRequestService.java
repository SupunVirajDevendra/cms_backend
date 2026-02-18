package com.epic.cms.service;

import com.epic.cms.dto.ActionDto;
import com.epic.cms.dto.CardRequestResponseDto;
import com.epic.cms.dto.CreateCardRequestDto;
import com.epic.cms.dto.PageResponse;

import java.util.List;

public interface CardRequestService {

    void createRequest(CreateCardRequestDto dto);

    void processRequest(Long requestId, ActionDto action);

    List<CardRequestResponseDto> getAllRequests();

    PageResponse<CardRequestResponseDto> getAllRequests(int page, int size);

    CardRequestResponseDto getRequestById(Long requestId);
}
