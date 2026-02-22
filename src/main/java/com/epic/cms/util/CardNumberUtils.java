package com.epic.cms.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class CardNumberUtils {
    
    private static final String MASK_CHARACTER = "*";
    
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return cardNumber;
        }
        
        // If it looks like Base64/encrypted (contains letters, +, /, = and is long), don't mask it
        // because masking an encrypted string is useless and makes it un-decryptable later if needed
        if (cardNumber.length() > 20 || cardNumber.matches(".*[a-zA-Z+/=].*")) {
            return cardNumber;
        }
        
        int firstDigits = 6;
        int lastDigits = 4;
        int middleLength = cardNumber.length() - firstDigits - lastDigits;
        
        if (middleLength <= 0) {
            return cardNumber;
        }
        
        String firstPart = cardNumber.substring(0, firstDigits);
        String lastPart = cardNumber.substring(cardNumber.length() - lastDigits);
        String maskedMiddle = MASK_CHARACTER.repeat(middleLength);
        
        return firstPart + maskedMiddle + lastPart;
    }
    
    public static String generateMaskId(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hexadecimal and take first 8 characters for a short ID
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "MASK_" + hexString.toString().substring(0, 8).toUpperCase();
            
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 not available
            return "MASK_" + String.valueOf(cardNumber.hashCode()).replace("-", "N");
        }
    }
}
