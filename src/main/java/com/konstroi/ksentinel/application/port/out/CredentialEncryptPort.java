package com.konstroi.ksentinel.application.port.out;

public interface CredentialEncryptPort {

    String encrypt(String plainText);

    String decrypt(String cipherText);
}
