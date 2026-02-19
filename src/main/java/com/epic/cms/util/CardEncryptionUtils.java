package com.epic.cms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.UUID;

@Component
public class CardEncryptionUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(CardEncryptionUtils.class);
    
    @Value("${cms.security.encryption.algorithm}")
    private String algorithm;
    
    @Value("${cms.security.encryption.transformation}")
    private String transformation;
    
    @Value("${cms.security.encryption.key}")
    private String secretKey;
    
    public CardEncryptionUtils() {
        logger.info("CardEncryptionUtils initialized");
        logger.debug("Encryption configuration: algorithm={}, transformation={}", algorithm, transformation);
    }
    
    public String encryptCardNumber(String cardNumber) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("encryptCardNumber() - Starting encryption for card number: {}", maskCardNumber(cardNumber));
        long startTime = System.currentTimeMillis();
        
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(encryptedBytes);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("encryptCardNumber() - Encryption completed in {}ms", duration);
            logger.info("encryptCardNumber() - Card number encrypted successfully: {}", maskCardNumber(cardNumber));
            
            return result;
        } catch (Exception e) {
            logger.error("encryptCardNumber() - Error encrypting card number {}: {}", maskCardNumber(cardNumber), e.getMessage(), e);
            throw new RuntimeException("Error encrypting card number", e);
        } finally {
            MDC.clear();
        }
    }
    
    public String decryptCardNumber(String encryptedCardNumber) {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("decryptCardNumber() - Starting decryption");
        long startTime = System.currentTimeMillis();
        
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            String result = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("decryptCardNumber() - Decryption completed in {}ms", duration);
            logger.info("decryptCardNumber() - Card number decrypted successfully: {}", maskCardNumber(result));
            
            return result;
        } catch (Exception e) {
            logger.error("decryptCardNumber() - Error decrypting card number: {}", e.getMessage(), e);
            throw new RuntimeException("Error decrypting card number", e);
        } finally {
            MDC.clear();
        }
    }
    
    public String generateSecureKey() {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("generateSecureKey() - Starting secure key generation");
        long startTime = System.currentTimeMillis();
        
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            keyGenerator.init(256, new SecureRandom());
            SecretKey generatedKey = keyGenerator.generateKey();
            String result = Base64.getEncoder().encodeToString(generatedKey.getEncoded());
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("generateSecureKey() - Key generation completed in {}ms", duration);
            logger.info("generateSecureKey() - Secure key generated successfully (length: {})", result.length());
            
            return result;
        } catch (Exception e) {
            logger.error("generateSecureKey() - Error generating secure key: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating secure key", e);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Masks card number for logging purposes (show only first 4 and last 4 digits)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
