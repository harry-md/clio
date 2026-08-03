package com.harry.clio.service;

import java.nio.file.Path;
import java.time.Instant;

import javax.crypto.SecretKey;

public interface CryptoService {
    SecretKey generateContentKey();

    Path encryptFile(Path originFile, SecretKey contentKey);

    String encryptContentKey(SecretKey contentKey);

    String createLicense(
            Integer userId, Integer bookId, String encryptedContentKey, String publicKeySpki);

    String createLicense(
            Integer userId,
            Integer bookId,
            Integer subId,
            Instant expiredAt,
            String encryptedContentKey,
            String publicKeySpki);
}
