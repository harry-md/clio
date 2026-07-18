package com.harry.clio.service.impl;

import com.harry.clio.entity.Book;
import com.harry.clio.entity.BookInfo;
import com.harry.clio.entity.BookStatus;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.repository.BookInfoRepository;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.service.BookProcessingService;
import com.harry.clio.service.CloudinaryService;
import com.harry.clio.service.R2Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookProcessingServiceImpl implements BookProcessingService {
    private final R2Service r2Service;
    private final CloudinaryService cloudinaryService;
    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final TransactionTemplate transactionTemplate;

    private record EpubExtractData(long wordCount, byte[] coverImage) {}

    @Value("${clio.master_key}")
    private String MASTER_KEY;

    private static final int AES_KEY_LENGTH = 256;
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void process(Integer bookId) {
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        Path cleanFile = null;
        Path encryptedFile = null;
        String encryptedFileUrl = null;
        String thumbnailUrl = null;
        try {
            cleanFile = r2Service.downloadToTemp(book.getFileUrl());
            EpubExtractData extractResult = extractEpubData(cleanFile);
            long wordCount = extractResult.wordCount();
            if (extractResult.coverImage() != null) {
                thumbnailUrl = cloudinaryService.upload(extractResult.coverImage());
            }
            SecretKey contentKey = generateContentKey();
            encryptedFile = encryptFile(cleanFile, contentKey);

            encryptedFileUrl = r2Service.uploadEncryptedEbook(encryptedFile);
            String encryptedContentKey = encryptContentKey(contentKey);

            final long finalWordCount = wordCount;
            final String finalEncryptedFileUrl = encryptedFileUrl;
            final String finalEncryptedContentKey = encryptedContentKey;
            final String finalThumbnailUrl = thumbnailUrl;
            transactionTemplate.executeWithoutResult(status -> {
                Book managedBook = bookRepository.getReferenceById(bookId);
                managedBook.setEncryptedFileUrl(finalEncryptedFileUrl);
                managedBook.setEncryptedContentKey(finalEncryptedContentKey);
                managedBook.setStatus(BookStatus.COMPLETED);
                if (finalThumbnailUrl != null) {
                    managedBook.setThumbnail(finalThumbnailUrl);
                }

                BookInfo bookInfo = bookInfoRepository.getReferenceById(bookId);
                bookInfo.setWordCount(finalWordCount);
            });

        } catch (IOException | RuntimeException ex) {
            log.error("Lỗi xử lý sách {}", bookId, ex);
            if (encryptedFileUrl != null) {
                r2Service.delete(encryptedFileUrl);
            }
            if (thumbnailUrl != null) {
                cloudinaryService.delete(thumbnailUrl);
            }
            transactionTemplate.executeWithoutResult(status -> {
                Book managedBook = bookRepository.getReferenceById(bookId);
                managedBook.setStatus(BookStatus.FAILED);
            });
        } finally {
            deleteTmpFile(cleanFile);
            deleteTmpFile(encryptedFile);
        }
    }

    private EpubExtractData extractEpubData(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            nl.siegmann.epublib.domain.Book book = new EpubReader().readEpub(inputStream);
            long wordCount = 0;
            for (Resource resource : book.getContents()) {
                String htmlContent = new String(resource.getData(), StandardCharsets.UTF_8);
                String content = Jsoup.parse(htmlContent).text();
                if (!content.trim().isEmpty()) {
                    wordCount += content.trim().split("\\s+").length;
                }
            }
            byte[] coverImage = null;
            Resource coverResource = book.getCoverImage();
            if (coverResource != null) {
                coverImage = coverResource.getData();
            }
            return new EpubExtractData(wordCount, coverImage);
        }
    }

    private SecretKey generateContentKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_LENGTH, new SecureRandom());
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Lỗi tạo contentKey", ex);
        }
    }

    private Path encryptFile(Path originFile, SecretKey contentKey) {
        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        new SecureRandom().nextBytes(nonce);

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
            throw new RuntimeException("Lỗi mã hóa epub", ex);
        }
    }

    private String encryptContentKey(SecretKey contentKey) {
        try {
            byte[] masterKey = Base64.getDecoder().decode(MASTER_KEY);
            SecretKey masterSecretKey = new SecretKeySpec(masterKey, "AES");

            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    masterSecretKey,
                    new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            byte[] cipherText = cipher.doFinal(contentKey.getEncoded());

            ByteBuffer buffer = ByteBuffer.allocate(nonce.length + cipherText.length);
            buffer.put(nonce).put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("Lỗi mã hóa contentKey", ex);
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
}
