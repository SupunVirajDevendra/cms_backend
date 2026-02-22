package com.epic.cms.util;

import com.epic.cms.model.Card;
import com.epic.cms.repository.CardRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CardNumberResolver {

    private final CardRepository cardRepository;

    public CardNumberResolver(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Find card by masked card number
     * Example: "1234********3456" → finds card with real number "1234567890123456"
     */
    public Optional<Card> findByMaskedCardNumber(String maskedCardNumber) {
        if (maskedCardNumber == null || !maskedCardNumber.contains("*")) {
            return Optional.empty();
        }

        try {
            // Extract first 4 and last 4 digits
            String firstFour = maskedCardNumber.substring(0, 4);
            String lastFour = maskedCardNumber.substring(maskedCardNumber.length() - 4);

            // Find all cards and match pattern
            List<Card> allCards = cardRepository.findAll();
            
            return allCards.stream()
                    .filter(card -> {
                        String cardNum = card.getCardNumber();
                        return cardNum != null && 
                               cardNum.startsWith(firstFour) && 
                               cardNum.endsWith(lastFour);
                    })
                    .findFirst();
        } catch (StringIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    /**
     * Find card by mask ID
     * Example: "MASK_A1B2C3D4" → finds corresponding card
     */
    public Optional<Card> findByMaskId(String maskId) {
        if (maskId == null || !maskId.startsWith("MASK_")) {
            return Optional.empty();
        }

        List<Card> allCards = cardRepository.findAll();
        
        return allCards.stream()
                .filter(card -> {
                    String cardMaskId = CardNumberUtils.generateMaskId(card.getCardNumber());
                    return cardMaskId.equals(maskId);
                })
                .findFirst();
    }

    /**
     * Resolve card number from various input formats
     * Accepts: plain card number, masked card number, or mask ID
     */
    public Optional<Card> resolveCard(String cardInput) {
        if (cardInput == null || cardInput.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmedInput = cardInput.trim();

        // 1. Try plain card number first (the service already encrypts it for lookup)
        try {
            Optional<Card> card = cardRepository.findByCardNumber(trimmedInput);
            if (card.isPresent()) {
                return card;
            }
        } catch (Exception e) {
            // Continue
        }

        // 2. Try mask ID
        if (trimmedInput.startsWith("MASK_")) {
            return findByMaskId(trimmedInput);
        }

        // 3. Try masked card number
        if (trimmedInput.contains("*")) {
            return findByMaskedCardNumber(trimmedInput);
        }

        // 4. Final attempt: Maybe it's already an encrypted string from the DB
        // But we shouldn't really have those in the input unless it's a bug
        
        return Optional.empty();
    }
}
