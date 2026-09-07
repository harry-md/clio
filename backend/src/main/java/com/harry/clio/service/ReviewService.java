package com.harry.clio.service;

import com.harry.clio.dto.review.AdminReviewResponse;
import com.harry.clio.dto.review.ReviewRequest;
import com.harry.clio.dto.review.ReviewResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewService {
    Optional<ReviewResponse> getMyReview(int userId, int bookId);

    Page<ReviewResponse> getAllReviews(int bookId, Pageable pageable);

    Page<AdminReviewResponse> adminGetAllReviews(String keyword, Pageable pageable);

    ReviewResponse review(int userId, int bookId, ReviewRequest request);

    ReviewResponse updateReview(int userId, int bookId, ReviewRequest request);

    void deleteReview(int userId, int bookId);

    void adminDeleteReview(int reviewId);
}
