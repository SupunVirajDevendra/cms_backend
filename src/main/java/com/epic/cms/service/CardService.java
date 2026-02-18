package com.epic.cms.service;

import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.model.Card;

import java.util.List;

public interface CardService {

    List<Card> getAllCards();

    void createCard(CreateCardDto dto);
}

