package com.harry.clio.service.impl;

import com.harry.clio.dto.review.ReviewRequest;
import com.harry.clio.dto.review.ReviewResponse;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.ReviewMapper;
import com.harry.clio.model.BookStatus;
import com.harry.clio.model.BookType;
import com.harry.clio.model.Review;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.repository.ReviewRepository;
import com.harry.clio.repository.UserLibraryRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserLibraryRepository userLibraryRepository;

    @Override
    public Optional<ReviewResponse> getMyReview(int userId, int bookId) {
        return reviewRepository
                .findWithUserByUserIdAndBookId(userId, bookId)
                .map(reviewMapper::toResponse);
    }

    @Override
    public Page<ReviewResponse> getAllReviews(int bookId, Pageable pageable) {
        return reviewRepository.findAllByBookId(bookId, pageable).map(reviewMapper::toResponse);
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = "books", allEntries = true),
                @CacheEvict(cacheNames = "book-detail", key = "#bookId")
            })
    @Transactional
    @Override
    public ReviewResponse review(int userId, int bookId, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("Bạn đã đánh giá rồi");
        }
        if (!userLibraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("Bạn không có quyền đánh giá");
        }

        int rating = request.rating();
        int updatedRow = bookRepository.updateBookRating(
                bookId, rating, 1, BookStatus.COMPLETED, BookType.SYSTEM);
        if (updatedRow != 1) {
            throw new RuntimeException("Có lỗi khi cập nhật rating cho sách");
        }
        return reviewMapper.toResponse(reviewRepository.save(Review.builder()
                .user(userRepository.getReferenceById(userId))
                .book(bookRepository.getReferenceById(bookId))
                .rating(rating)
                .comment(request.comment())
                .build()));
    }

    @Transactional
    @Override
    public ReviewResponse updateReview(int userId, int bookId, ReviewRequest request) {
        if (!userLibraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("Bạn không có quyền đánh giá");
        }

        Review review = reviewRepository
                .findWithUserByUserIdAndBookId(userId, bookId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy đánh giá cũ của bạn"));
        int oldValue = review.getRating();

        reviewMapper.updateReview(review, request);

        int updatedRow = bookRepository.updateBookRating(
                bookId, review.getRating() - oldValue, 0, BookStatus.COMPLETED, BookType.SYSTEM);
        if (updatedRow != 1) {
            throw new RuntimeException("Có lỗi khi cập nhật rating cho sách");
        }

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Transactional
    @Override
    public void deleteReview(int userId, int bookId) {
        Review review = reviewRepository
                .findWithUserByUserIdAndBookId(userId, bookId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy đánh giá cũ của bạn"));
        int updatedRow = bookRepository.updateBookRating(
                bookId, -review.getRating(), -1, BookStatus.COMPLETED, BookType.SYSTEM);
        if (updatedRow != 1) {
            throw new RuntimeException("Có lỗi khi cập nhật rating cho sách");
        }

        reviewRepository.delete(review);
    }
}
