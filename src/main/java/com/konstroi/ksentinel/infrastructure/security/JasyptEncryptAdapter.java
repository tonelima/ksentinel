package com.konstroi.ksentinel.infrastructure.security;

import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JasyptEncryptAdapter implements CredentialEncryptPort {

    private final StringEncryptor stringEncryptor;

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        return stringEncryptor.encrypt(plainText);
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        // If not encrypted (e.g. plain text in tests), return as-is
        try {
            return stringEncryptor.decrypt(cipherText);
        } catch (Exception e) {
            return cipherText;
        }
    }
}
