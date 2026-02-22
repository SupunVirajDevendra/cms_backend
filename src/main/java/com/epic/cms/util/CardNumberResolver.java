package com.epic.cms.util;

import com.epic.cms.model.Card;
import com.epic.cms.repository.CardRepository;
import com.epic.cms.service.CardEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CardNumberResolver {

    private final CardRepository cardRepository;
    private final CardEncryptionService encryptionService;
    private static final Logger logger = LoggerFactory.getLogger(CardNumberResolver.class);

    public CardNumberResolver(CardRepository cardRepository, CardEncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.encryptionService = encryptionService;
    }

    public Optional<Card> findByMaskedCardNumber(String maskedCardNumber) {
        if (maskedCardNumber == null || !maskedCardNumber.contains("*")) {
            return Optional.empty();
        }

        try {
            String firstFour = maskedCardNumber.substring(0, 4);
            String lastFour = maskedCardNumber.substring(maskedCardNumber.length() - 4);

            List<Card> allCards = cardRepository.findAll();
            
            for (Card card : allCards) {
                try {
                    String decryptedNumber = encryptionService.decrypt(card.getCardNumber());
                    if (decryptedNumber.startsWith(firstFour) && decryptedNumber.endsWith(lastFour)) {
                        card.setCardNumber(decryptedNumber);
                        return Optional.of(card);
                    }
                } catch (Exception e) {
                    logger.error("Error decrypting card number: {}", card.getCardNumber(), e);
                }
            }
            return Optional.empty();
        } catch (StringIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Optional<Card> findByMaskId(String maskId) {
        if (maskId == null || !maskId.startsWith("MASK_")) {
            return Optional.empty();
        }

        List<Card> allCards = cardRepository.findAll();
        
        for (Card card : allCards) {
            try {
                String decryptedNumber = encryptionService.decrypt(card.getCardNumber());
                String cardMaskId = CardNumberUtils.generateMaskId(decryptedNumber);
                if (cardMaskId.equals(maskId)) {
                    card.setCardNumber(decryptedNumber);
                    return Optional.of(card);
                }
            } catch (Exception e) {
                logger.error("Error decrypting card number: {}", card.getCardNumber(), e);
            }
        }
        return Optional.empty();
    }

    public Optional<Card> resolveCard(String cardInput) {
        if (cardInput == null || cardInput.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmedInput = cardInput.trim();

        try {
            String encryptedInput = encryptionService.encrypt(trimmedInput);
            Optional<Card> card = cardRepository.findByCardNumber(encryptedInput);
            if (card.isPresent()) {
                card.get().setCardNumber(encryptionService.decrypt(card.get().getCardNumber()));
                return card;
            }
        } catch (Exception e) {
            logger.debug("Input is not a plain card number, trying other methods");
        }

        if (trimmedInput.startsWith("MASK_")) {
            return findByMaskId(trimmedInput);
        }

        if (trimmedInput.contains("*")) {
            return findByMaskedCardNumber(trimmedInput);
        }

        return Optional.empty();
    }
}
