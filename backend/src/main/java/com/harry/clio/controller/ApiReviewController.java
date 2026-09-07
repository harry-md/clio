package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.review.ReviewRequest;
import com.harry.clio.dto.review.ReviewResponse;
import com.harry.clio.service.ReviewService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
@RequiredArgsConstructor
public class ApiReviewController {
    private final ReviewService reviewService;

    @GetMapping("/my-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> retrieve(
            @PathVariable int bookId, @AuthenticationPrincipal CustomUser principal) {
        return reviewService
                .getMyReview(principal.getId(), bookId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> list(
            @PathVariable int bookId,
            @PageableDefault(size = 1, sort = "id", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(bookId, pageable));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> create(
            @PathVariable int bookId,
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.review(principal.getId(), bookId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/my-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> update(
            @PathVariable int bookId,
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(principal.getId(), bookId, request));
    }

    @DeleteMapping("/my-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable int bookId, @AuthenticationPrincipal CustomUser principal) {
        reviewService.deleteReview(principal.getId(), bookId);
        return ResponseEntity.noContent().build();
    }
}
