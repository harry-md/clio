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
        if (!subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new BadRequestException("Bạn không có subscription");
        }

        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));
        User user = userRepository.getReferenceById(userId);
        UserLibrary library = UserLibrary.builder()
                .user(user)
                .book(book)
                .type(UserLibraryType.SUBSCRIBED)
                .build();
        try {
            userLibraryRepository.save(library);
            return userLibraryMapper.toResponse(library, UserLibraryType.SUBSCRIBED, null, book);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Sách đã tồn tại trong thư viện");
        }
    }
}
