package com.harry.clio.service.impl;

import com.harry.clio.entity.Book;
import com.harry.clio.entity.BookInfo;
import com.harry.clio.entity.BookStatus;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.repository.BookInfoRepository;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.service.BookProcessingService;
import com.harry.clio.service.CryptoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import org.jsoup.Jsoup;
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
public class BookProcessingServiceImpl implements BookProcessingService {
    private final R2Service r2Service;
    private final CloudinaryService cloudinaryService;
    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final CryptoService cryptoService;
    private final TransactionTemplate transactionTemplate;

    private record EpubExtractData(long wordCount, byte[] coverImage) {}

    @Override
    public void process(Integer bookId) {
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));
        if (book.getStatus() == BookStatus.COMPLETED) {
            return;
        }

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
            SecretKey contentKey = cryptoService.generateContentKey();
            encryptedFile = cryptoService.encryptFile(cleanFile, contentKey);

            encryptedFileUrl = r2Service.uploadEncryptedEbook(encryptedFile);
            String encryptedContentKey = cryptoService.encryptContentKey(contentKey);

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
            bookRepository.updateStatus(bookId, BookStatus.FAILED);
            throw new RuntimeException("Xử lý sách thất bại", ex);
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
