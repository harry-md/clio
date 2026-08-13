package com.harry.clio.service.impl;

import com.harry.clio.dto.book.DownloadRequest;
import com.harry.clio.dto.book.DownloadResponse;
import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.entity.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.UserLibraryMapper;
import com.harry.clio.repository.*;
import com.harry.clio.service.CryptoService;
import com.harry.clio.service.UserLibraryService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserLibraryServiceImpl implements UserLibraryService {
    private final UserLibraryRepository userLibraryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserLibraryMapper userLibraryMapper;
    private final R2Service r2Service;
    private final CryptoService cryptoService;
    private final BookInfoRepository bookInfoRepository;
    private final SubscriptionBookBillingRepository subscriptionBookBillingRepository;

    @Transactional
    @Override
    public LibraryResponse addToLibrary(Integer userId, Integer bookId) {
        Optional<UserLibrary> existingLibrary =
                userLibraryRepository.findByUserIdAndBookId(userId, bookId);
        if (existingLibrary.isPresent()) {
            return userLibraryMapper.toResponse(existingLibrary.get());
        }

        if (!subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("Bạn không có subscription");
        }

        Book book = bookRepository
                .findAddableBookById(bookId, BookStatus.COMPLETED, BookType.SYSTEM)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sách không thể thêm vào thư viện"));

        User user = userRepository.getReferenceById(userId);

        UserLibrary library = UserLibrary.builder()
                .user(user)
                .book(book)
                .type(UserLibraryType.SUBSCRIBED)
                .build();

        if (subscriptionBookBillingRepository.existsByUserIdAndBookId(userId, bookId)) {
            return userLibraryMapper.toResponse(userLibraryRepository.save(library));
        }

        BookInfo info = bookInfoRepository
                .findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sách"));

        double pageCount = Math.ceil((double) info.getWordCount() / 250);

        SubscriptionBookBilling billing = SubscriptionBookBilling.builder()
                .user(user)
                .book(book)
                .pageCount((long) pageCount)
                .build();
        subscriptionBookBillingRepository.save(billing);

        return userLibraryMapper.toResponse(userLibraryRepository.save(library));
    }

    @Override
    public Page<LibraryResponse> getUserLibraries(int userId, Pageable pageable) {
        return userLibraryRepository
                .findAllByUserId(userId, pageable)
                .map(userLibraryMapper::toResponse);
    }

    @Override
    public DownloadResponse downloadBook(int userId, DownloadRequest request) {
        int bookId = request.bookId();
        UserLibrary library = userLibraryRepository
                .findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new BadRequestException("Chưa có sách này trong thư viện"));

        Book book = library.getBook();

        String license;
        switch (library.getType()) {
            case PURCHASED -> {
                license = cryptoService.createLicense(
                        userId, bookId, book.getEncryptedContentKey(), request.publicKeySpki());
            }
            case SUBSCRIBED -> {
                Subscription sub = subscriptionRepository
                        .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                        .orElseThrow(() -> new BadRequestException("Chưa có gói đăng ký hợp lệ"));

                license = cryptoService.createLicense(
                        userId,
                        bookId,
                        sub.getId(),
                        sub.getEndDate()
                                .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"))
                                .toInstant(),
                        book.getEncryptedContentKey(),
                        request.publicKeySpki());
            }
            default -> throw new BadRequestException("Sách không hợp lệ");
        }
        String downloadUrl = r2Service.getPresignedUrl(book.getEncryptedFileUrl());
        Instant urlExpiredAt = Instant.now().plus(Duration.ofMinutes(5));
        return new DownloadResponse(downloadUrl, urlExpiredAt, license);
    }
}
