package com.epic.cms.service.impl;

import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.model.Card;
import com.epic.cms.repository.CardRepository;
import com.epic.cms.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository repository;
    private static final Logger logger =
            LoggerFactory.getLogger(CardServiceImpl.class);

    public CardServiceImpl(CardRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Card> getAllCards() {
        return repository.findAll();
    }

    @Override
    public void createCard(CreateCardDto dto) {

        Card card = new Card();
        card.setCardNumber(dto.getCardNumber());
        card.setExpiryDate(dto.getExpiryDate());
        card.setStatusCode("IACT");
        card.setCreditLimit(dto.getCreditLimit());
        card.setCashLimit(dto.getCashLimit());
        card.setAvailableCreditLimit(dto.getCreditLimit());
        card.setAvailableCashLimit(dto.getCashLimit());
        card.setLastUpdateTime(LocalDateTime.now());

        repository.save(card);

        logger.info("Card created: {}", dto.getCardNumber());
    }
}

