package com.harry.clio.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class CryptoConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    public SecretKey masterKey(@Value("${clio.master-key}") String masterKey) {
        return new SecretKeySpec(Base64.getDecoder().decode(masterKey), "AES");
    }
}
