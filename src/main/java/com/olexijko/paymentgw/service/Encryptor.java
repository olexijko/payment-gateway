package com.olexijko.paymentgw.service;

import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class Encryptor {
    public String encrypt(String value) {
        return value == null ? null : Base64.getEncoder().encodeToString(value.getBytes());
    }

    public String decrypt(String value) {
        return value == null ? null : new String(Base64.getDecoder().decode(value.getBytes()));
    }
}
