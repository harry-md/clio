package com.harry.clio.service.impl;

import com.harry.clio.dto.book.DownloadRequest;
import com.harry.clio.dto.book.DownloadResponse;
import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.dto.library.LicenseResponse;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.UserLibraryMapper;
import com.harry.clio.model.*;
import com.harry.clio.repository.*;
import com.harry.clio.service.CryptoService;
import com.harry.clio.service.UserLibraryService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Transactional
    @Override
    public LibraryResponse addToLibrary(Integer userId, Integer bookId) {
        Optional<UserLibrary> existingLibrary =
                userLibraryRepository.findWithBookByUserIdAndBookId(userId, bookId);
        if (existingLibrary.isPresent()) {
            return userLibraryMapper.toResponse(existingLibrary.get());
        }

        if (!subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("Bạn không có subscription");
        }

        Book book = bookRepository
                .findAvailableBookById(bookId, BookStatus.COMPLETED, BookType.SYSTEM)
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
                .findAllWithBookByUserId(userId, pageable)
                .map(userLibraryMapper::toResponse);
    }

    private UserLibrary getLibraryOrThrow(int userId, int bookId) {
        return userLibraryRepository
                .findWithBookByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new BadRequestException("Chưa có sách này trong thư viện"));
    }

    @Override
    public DownloadResponse downloadBook(int userId, DownloadRequest request) {
        UserLibrary library = getLibraryOrThrow(userId, request.bookId());

        String license = createCurrentLicense(userId, library, request.publicKeySpki());
        String downloadUrl = r2Service.getPresignedUrl(library.getBook().getEncryptedFileUrl());
        return new DownloadResponse(downloadUrl, license);
    }

    @Override
    public LicenseResponse refreshLicense(int userId, int bookId, String publicKeySpki) {
        UserLibrary library = getLibraryOrThrow(userId, bookId);
        return new LicenseResponse(createCurrentLicense(userId, library, publicKeySpki));
    }

    private String createCurrentLicense(int userId, UserLibrary library, String publicKeySpki) {
        Book book = library.getBook();
        int bookId = book.getId();

        switch (library.getType()) {
            case PURCHASED -> {
                return cryptoService.createLicense(
                        userId, bookId, book.getEncryptedContentKey(), publicKeySpki);
            }

            case SUBSCRIBED -> {
                ZoneId zone = ZoneId.of(zoneId);
                LocalDate currentDate = LocalDate.now(zone);

                Subscription subscription = subscriptionRepository
                        .findActiveSubscriptionByUserId(
                                userId, SubscriptionStatus.ACTIVE, currentDate)
                        .orElseThrow(() -> new BadRequestException("Gói đọc sách đã hết hạn"));

                return cryptoService.createLicense(
                        userId,
                        bookId,
                        subscription.getId(),
                        subscription.getEndDate().atStartOfDay(zone).toInstant(),
                        book.getEncryptedContentKey(),
                        publicKeySpki);
            }
            default -> throw new BadRequestException("Sách không hợp lệ");
        }
    }
}
