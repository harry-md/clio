package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.BookFilterRequest;
import com.harry.clio.dto.book.BookListResponse;
import com.harry.clio.dto.book.CreateBookMetadataRequest;
import com.harry.clio.service.BookService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class ApiBookController {
    private final BookService bookService;

    @PreAuthorize("hasRole('PUBLISHER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDetailResponse> create(
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestPart("metadata") CreateBookMetadataRequest request,
            @RequestPart("file") MultipartFile file) {
        BookDetailResponse response = bookService.uploadBook(principal.getId(), request, file);
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
    public ResponseEntity<BookDetailResponse> getBookDetail(@PathVariable Integer bookId) {
        return ResponseEntity.ok(bookService.getBookDetail(bookId));
    }
}
