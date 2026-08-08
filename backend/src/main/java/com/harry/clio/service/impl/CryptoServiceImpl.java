package com.harry.clio.service.impl;

import com.harry.clio.entity.LicenseType;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.CryptoException;
import com.harry.clio.service.CryptoService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@RequiredArgsConstructor
@Service
public class CryptoServiceImpl implements CryptoService {
    private final SecureRandom secureRandom;
    private final SecretKey masterKey;

    @Value("${clio.license-key}")
    private String licenseKey;

    private static final int AES_KEY_LENGTH = 256;
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Override
    public SecretKey generateContentKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_LENGTH, secureRandom);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new CryptoException("Lỗi tạo contentKey", ex);
        }
    }

    @Override
    public Path encryptFile(Path originFile, SecretKey contentKey) {
        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        secureRandom.nextBytes(nonce);

        Path encryptedFile = null;
        try {
            encryptedFile = Files.createTempFile("encrypted-", "");
            try (InputStream inputStream = Files.newInputStream(originFile);
                    OutputStream outputStream = Files.newOutputStream(encryptedFile)) {
                outputStream.write(nonce);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        contentKey,
                        new GCMParameterSpec(GCM_TAG_LENGTH, nonce));

                try (CipherOutputStream cipherOut = new CipherOutputStream(outputStream, cipher)) {
                    inputStream.transferTo(cipherOut);
                }
                return encryptedFile;
            }
        } catch (IOException | GeneralSecurityException ex) {
            deleteTmpFile(encryptedFile);
            throw new CryptoException("Lỗi mã hóa epub", ex);
        }
    }

    private void deleteTmpFile(Path tmpFile) {
        if (tmpFile != null) {
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException ex) {
                log.error("Lỗi xóa file tạm", ex);
            }
        }
    }

    @Override
    public String encryptContentKey(SecretKey contentKey) {
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] cipherText = cipher.doFinal(contentKey.getEncoded());

            ByteBuffer buffer = ByteBuffer.allocate(nonce.length + cipherText.length);
            buffer.put(nonce).put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Lỗi mã hóa contentKey", ex);
        }
    }

    @Override
    public String createLicense(
            Integer userId, Integer bookId, String encryptedContentKey, String publicKeySpki) {
        PublicKey publicKey = parsePublicKey(publicKeySpki);
        SecretKey contentKey = decryptContentKey(encryptedContentKey);
        String wrappedContentKey = wrapContentKey(contentKey, publicKey);
        return signLicense(userId, bookId, wrappedContentKey);
    }

    @Override
    public String createLicense(
            Integer userId,
            Integer bookId,
            Integer subId,
            Instant expiredAt,
            String encryptedContentKey,
            String publicKeySpki) {
        PublicKey publicKey = parsePublicKey(publicKeySpki);
        SecretKey contentKey = decryptContentKey(encryptedContentKey);
        String wrappedContentKey = wrapContentKey(contentKey, publicKey);
        return signLicense(userId, bookId, subId, expiredAt, wrappedContentKey);
    }

    private PublicKey parsePublicKey(String publicKeySpki) {
        try {
            KeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeySpki));
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
            if (!(publicKey instanceof RSAPublicKey rsaPublicKey)
                    || rsaPublicKey.getModulus().bitLength() < 2048) {
                throw new BadRequestException("Public key không hợp lệ");
            }
            return publicKey;
        } catch (InvalidKeySpecException | IllegalArgumentException | NoSuchAlgorithmException ex) {
            throw new BadRequestException("Lỗi lấy public key", ex);
        }
    }

    private SecretKey decryptContentKey(String encryptedContentKey) {
        byte[] decoded = Base64.getDecoder().decode(encryptedContentKey);
        byte[] nonce = Arrays.copyOf(decoded, GCM_NONCE_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(decoded, GCM_NONCE_LENGTH, decoded.length);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH, nonce));

            byte[] contentKeyBytes = cipher.doFinal(cipherText);
            return new SecretKeySpec(contentKeyBytes, "AES");
        } catch (Exception ex) {
            throw new CryptoException("Lỗi giải mã contentKey", ex);
        }
    }

    private String wrapContentKey(SecretKey contentKey, PublicKey publicKey) {
        try {
            OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec(
                    "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.WRAP_MODE, publicKey, oaepParameterSpec);

            byte[] wrappedKey = cipher.doFinal(contentKey.getEncoded());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(wrappedKey);
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Lỗi wrap contentKey", ex);
        }
    }

    private PrivateKey parseLicensePrivateKey(String pem) {
        String encodedKey = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new CryptoException("License private key không hợp lệ", ex);
        }
    }

    private String signLicense(Integer userId, Integer bookId, String wrappedContentKey) {
        try {
            String pem = new String(Base64.getDecoder().decode(licenseKey));
            PrivateKey privateKey = parseLicensePrivateKey(pem);
            JWSSigner signer = new RSASSASigner(privateKey);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("bookId", bookId)
                    .claim("wrappedContentKey", wrappedContentKey)
                    .claim("licenseType", LicenseType.PURCHASED.name())
                    .issueTime(new Date())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException | IllegalArgumentException ex) {
            throw new CryptoException("Lỗi ký license", ex);
        }
    }

    private String signLicense(
            Integer userId,
            Integer bookId,
            Integer subId,
            Instant expiredAt,
            String wrappedContentKey) {
        try {
            String pem = new String(Base64.getDecoder().decode(licenseKey));
            PrivateKey privateKey = parseLicensePrivateKey(pem);
            JWSSigner signer = new RSASSASigner(privateKey);

            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("bookId", bookId)
                    .claim("subId", subId)
                    .claim("wrappedContentKey", wrappedContentKey)
                    .claim("licenseType", LicenseType.SUBSCRIPTION.name())
                    .issueTime(Date.from(now))
                    .claim(
                            "offlineUntil",
                            Math.min(
                                    now.plus(Duration.ofDays(7)).getEpochSecond(),
                                    expiredAt.getEpochSecond()))
                    .expirationTime(new Date(expiredAt.toEpochMilli()))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException | IllegalArgumentException ex) {
            throw new CryptoException("Lỗi ký license", ex);
        }
    }
}
