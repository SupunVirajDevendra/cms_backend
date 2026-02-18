package com.epic.cms.service.impl;

import com.epic.cms.dto.CardResponseDto;
import com.epic.cms.dto.CreateCardDto;
import com.epic.cms.dto.UpdateCardDto;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.mapper.DtoMapper;
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
    private final DtoMapper dtoMapper;
    private static final Logger logger =
            LoggerFactory.getLogger(CardServiceImpl.class);

    public CardServiceImpl(CardRepository repository, DtoMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CardResponseDto> getAllCards() {
        List<Card> cards = repository.findAll();
        return dtoMapper.toCardResponseDtoList(cards);
    }

    @Override
    public CardResponseDto getByCardNumber(String cardNumber) {
        Card card = repository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNumber));
        return dtoMapper.toCardResponseDto(card);
    }

    @Override
    public void createCard(CreateCardDto dto) {

        Card card = Card.builder()
                .cardNumber(dto.getCardNumber())
                .expiryDate(dto.getExpiryDate())
                .statusCode("IACT")
                .creditLimit(dto.getCreditLimit())
                .cashLimit(dto.getCashLimit())
                .availableCreditLimit(dto.getCreditLimit())
                .availableCashLimit(dto.getCashLimit())
                .lastUpdateTime(LocalDateTime.now())
                .build();

        repository.save(card);

        logger.info("Card created: {}", dto.getCardNumber());
    }

    @Override
    public void updateCard(String cardNumber, UpdateCardDto dto) {
        Card existingCard = repository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNumber));

        existingCard.setExpiryDate(dto.getExpiryDate());
        existingCard.setCreditLimit(dto.getCreditLimit());
        existingCard.setCashLimit(dto.getCashLimit());
        existingCard.setAvailableCreditLimit(dto.getCreditLimit());
        existingCard.setAvailableCashLimit(dto.getCashLimit());
        existingCard.setLastUpdateTime(LocalDateTime.now());

        repository.update(existingCard);

        logger.info("Card updated: {}", cardNumber);
    }
}

