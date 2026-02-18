package com.epic.cms.service;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.PageResponse;
import com.epic.cms.dto.UpdateCardDto;

import java.util.List;

public interface CardService {

    List<CardResponseDto> getAllCards();

    PageResponse<CardResponseDto> getAllCards(int page, int size);

    CardResponseDto getByCardNumber(String cardNumber);

    void createCard(CreateCardDto dto);

    void updateCard(String cardNumber, UpdateCardDto dto);
}

