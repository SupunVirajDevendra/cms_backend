package com.epic.cms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;

@Service
public class PayloadDecryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PayloadDecryptionService.class);
    
    @Value("${cms.security.encryption.key}")
    private String secretKey;
    
    @Value("${cms.security.encryption.salt:cms-salt-2024}")
    private String salt;
    
    private final ObjectMapper objectMapper;
    
    public PayloadDecryptionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        logger.info("PayloadDecryptionService initialized");
    }
    
    public String decrypt(String encryptedPayload) throws Exception {
        String operationId = UUID.randomUUID().toString();
        MDC.put("operationId", operationId);
        
        logger.debug("decrypt() - Starting payload decryption");
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Decode Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedPayload);
            logger.debug("decrypt() - Base64 decoded, length: {} bytes", decoded.length);
            
            // 2. Extract IV (first 12 bytes) and CipherText (rest)
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);
            
            logger.debug("decrypt() - Extracted IV: {} bytes, ciphertext: {} bytes", iv.length, cipherText.length);
            
            // 3. Derive Key (PBKDF2WithHmacSHA256, 100000 iterations, 256-bit key)
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 100000, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            
            logger.debug("decrypt() - Key derived successfully");
            
            // 4. Decrypt (AES/GCM/NoPadding)
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag length
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
            byte[] plainText = cipher.doFinal(cipherText);
            
            String result = new String(plainText);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("decrypt() - Decryption completed in {}ms, result length: {} chars", duration, result.length());
            logger.info("decrypt() - Payload decrypted successfully");
            
            return result;
        } catch (Exception e) {
            logger.error("decrypt() - Error decrypting payload: {}", e.getMessage(), e);
            throw new RuntimeException("Payload decryption failed", e);
        } finally {
            MDC.clear();
        }
    }
    
    public <T> T decryptToObject(String encryptedPayload, Class<T> targetClass) throws Exception {
        String json = decrypt(encryptedPayload);
        return objectMapper.readValue(json, targetClass);
    }
}
