package com.harry.clio.service.impl;

import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.entity.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.UserLibraryMapper;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.repository.SubscriptionRepository;
import com.harry.clio.repository.UserLibraryRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.UserLibraryService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserLibraryServiceImpl implements UserLibraryService {
    private final UserLibraryRepository userLibraryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserLibraryMapper userLibraryMapper;

    @Override
    public LibraryResponse addToLibrary(Integer userId, Integer bookId) {
        Optional<UserLibrary> existing =
                userLibraryRepository.findByUserIdAndBookId(userId, bookId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        if (!subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("Bạn không có subscription");
        }

        Book book = bookRepository
                .findAddableBookById(bookId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sách không thể thêm vào thư viện"));
        UserLibrary library = UserLibrary.builder()
                .user(userRepository.getReferenceById(userId))
                .book(book)
                .type(UserLibraryType.SUBSCRIBED)
                .build();
        try {
            return toResponse(userLibraryRepository.saveAndFlush(library));
        } catch (DataIntegrityViolationException ex) {
            UserLibrary concurrentlyCreated = userLibraryRepository
                    .findByUserIdAndBookId(userId, bookId)
                    .orElseThrow(() -> ex);
            return toResponse(concurrentlyCreated);
        }
    }

    private LibraryResponse toResponse(UserLibrary library) {
        return userLibraryMapper.toResponse(
                library, library.getType(), library.getCfiPosition(), library.getBook());
    }
}
