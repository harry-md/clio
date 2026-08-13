package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.book.*;
import com.harry.clio.service.BookService;
import com.harry.clio.service.impl.R2Service;

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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class ApiBookController {
    private final BookService bookService;
    private final R2Service r2Service;

    @PreAuthorize("hasRole('PUBLISHER')")
    @PostMapping("/upload-url")
    public ResponseEntity<PresignedUpload> createUploadUrl() {
        return ResponseEntity.ok(r2Service.createOriginUploadUrl());
    }

    @PreAuthorize("hasRole('PUBLISHER')")
    @PostMapping
    public ResponseEntity<BookDetailResponse> create(
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody CreateBookMetadataRequest request) {
        BookDetailResponse response = bookService.uploadBook(principal.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<BookListResponse>> list(
            @Valid @ModelAttribute BookFilterRequest request,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(request, pageable));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailResponse> retrieve(@PathVariable Integer bookId) {
        return ResponseEntity.ok(bookService.getBookDetail(bookId));
    }
}
