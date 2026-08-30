package com.harry.clio.service.impl;

import com.harry.clio.exception.InvalidEbookException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.model.Book;
import com.harry.clio.model.BookStatus;
import com.harry.clio.repository.BookInfoRepository;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.service.BookProcessService;
import com.harry.clio.service.CryptoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import org.jsoup.Jsoup;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.crypto.SecretKey;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookProcessServiceImpl implements BookProcessService {
    private final R2Service r2Service;
    private final CloudinaryService cloudinaryService;
    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final CryptoService cryptoService;
    private final TransactionTemplate transactionTemplate;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    private record ExtractedData(long wordCount, byte[] coverImage) {}

    private record EncryptedData(
            String encryptedContentKey, Path encryptedFile, String encryptedFileUrl) {}

    @CacheEvict(cacheNames = "books", allEntries = true)
    @Override
    public void process(int bookId) {
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));
        if (book.getStatus() == BookStatus.COMPLETED || book.getStatus() == BookStatus.FAILED) {
            return;
        }

        Path cleanFile = null;
        Path encryptedFile = null;
        String encryptedFileUrl = null;
        String thumbnailUrl = null;
        try {
            cleanFile = r2Service.downloadToTemp(book.getFileUrl());

            final long fileSize = validate(cleanFile);

            ExtractedData data = extractEpubData(cleanFile);
            final long wordCount = data.wordCount();
            if (data.coverImage() != null) {
                thumbnailUrl = cloudinaryService.upload(data.coverImage());
            }

            EncryptedData encryptedData = encryptAndUpload(cleanFile);
            encryptedFile = encryptedData.encryptedFile();
            encryptedFileUrl = encryptedData.encryptedFileUrl();
            final String encryptedContentKey = encryptedData.encryptedContentKey;

            final String finalThumbnailUrl = thumbnailUrl;
            final String finalEncryptedFileUrl = encryptedFileUrl;
            transactionTemplate.executeWithoutResult(status -> {
                updateBookAndInfo(
                        bookId,
                        finalEncryptedFileUrl,
                        encryptedContentKey,
                        finalThumbnailUrl,
                        fileSize,
                        wordCount);
            });
        } catch (InvalidEbookException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            log.error("Lỗi xử lý sách {}", bookId, ex);
            if (encryptedFileUrl != null) {
                r2Service.delete(encryptedFileUrl);
            }
            if (thumbnailUrl != null) {
                cloudinaryService.delete(thumbnailUrl);
            }
            throw new RuntimeException("Xử lý sách thất bại", ex);
        } finally {
            deleteTmpFile(cleanFile);
            deleteTmpFile(encryptedFile);
        }
    }

    @Override
    public void handleBookFailed(int bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            r2Service.delete(book.getFileUrl());
            bookRepository.updateStatus(bookId, BookStatus.FAILED);
        }
    }

    private void updateBookAndInfo(
            int id,
            String encryptedFileUrl,
            String encryptedContentKey,
            String thumbnail,
            long fileSize,
            long wordCount) {
        int updatedBook = bookRepository.updateInfo(
                id, encryptedFileUrl, encryptedContentKey, thumbnail, BookStatus.COMPLETED);
        int updatedInfo = bookInfoRepository.updateInfo(id, fileSize, wordCount);
        if (updatedBook != 1 || updatedInfo != 1) {
            throw new RuntimeException("Lỗi khi cập nhật thông tin sách");
        }
    }

    private EncryptedData encryptAndUpload(Path cleanFile) {
        Path encryptedFile = null;
        try {
            SecretKey contentKey = cryptoService.generateContentKey();
            encryptedFile = cryptoService.encryptFile(cleanFile, contentKey);

            String encryptedContentKey = cryptoService.encryptContentKey(contentKey);
            String encryptedFileUrl = r2Service.uploadEncryptedEbook(encryptedFile);
            return new EncryptedData(encryptedContentKey, encryptedFile, encryptedFileUrl);
        } catch (RuntimeException ex) {
            deleteTmpFile(encryptedFile);
            throw ex;
        }
    }

    private ExtractedData extractEpubData(Path file) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file)) {
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
            return new ExtractedData(wordCount, coverImage);
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

    private long validate(Path file) throws IOException {
        long fileSize = Files.size(file);
        if (fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new InvalidEbookException("Kích thước file không hợp lệ");
        }
        r2Service.validateEpub(file);
        return fileSize;
    }
}
