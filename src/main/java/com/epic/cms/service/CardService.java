package com.epic.cms.service;

import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.model.Card;

import java.util.List;

public interface CardService {

    List<Card> getAllCards();

    Card getByCardNumber(String cardNumber);

    void createCard(CreateCardDto dto);

    void updateCard(String cardNumber, UpdateCardDto dto);
}

