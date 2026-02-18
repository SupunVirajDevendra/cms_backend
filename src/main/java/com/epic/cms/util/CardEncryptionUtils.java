package com.epic.cms.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;

@Component
public class CardEncryptionUtils {
    
    @Value("${cms.security.encryption.algorithm}")
    private String algorithm;
    
    @Value("${cms.security.encryption.transformation}")
    private String transformation;
    
    @Value("${cms.security.encryption.key}")
    private String secretKey;
    
    public String encryptCardNumber(String cardNumber) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }
    
    public String decryptCardNumber(String encryptedCardNumber) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }
    
    public String generateSecureKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            keyGenerator.init(256, new SecureRandom());
            SecretKey generatedKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(generatedKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error generating secure key", e);
        }
    }
}
