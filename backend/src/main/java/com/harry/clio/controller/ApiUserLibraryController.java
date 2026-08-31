package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.book.DownloadRequest;
import com.harry.clio.dto.book.DownloadResponse;
import com.harry.clio.dto.library.*;
import com.harry.clio.service.ReadingProgressService;
import com.harry.clio.service.UserLibraryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiUserLibraryController {
    private final UserLibraryService userLibraryService;
    private final ReadingProgressService readingProgressService;

    @PostMapping("/libraries")
    public ResponseEntity<LibraryResponse> addToLibrary(
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody LibraryRequest request) {
        return ResponseEntity.ok(
                userLibraryService.addToLibrary(principal.getId(), request.bookId()));
    }

    @GetMapping("/libraries")
    public ResponseEntity<Page<LibraryResponse>> list(
            @AuthenticationPrincipal CustomUser principal,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(userLibraryService.getUserLibraries(principal.getId(), pageable));
    }

    @PostMapping("/libraries/download")
    public ResponseEntity<DownloadResponse> download(
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody DownloadRequest request) {
        return ResponseEntity.ok(userLibraryService.downloadBook(principal.getId(), request));
    }

    @PostMapping("/libraries/{bookId}/license/refresh")
    public ResponseEntity<LicenseResponse> refreshLicense(
            @AuthenticationPrincipal CustomUser principal,
            @PathVariable int bookId,
            @Valid @RequestBody LicenseRefreshRequest request) {
        return ResponseEntity.ok(userLibraryService.refreshLicense(
                principal.getId(), bookId, request.publicKeySpki()));
    }

    @GetMapping("/libraries/{bookId}/progress")
    public ResponseEntity<ReadingProgressResponse> getProgress(
            @AuthenticationPrincipal CustomUser principal, @PathVariable int bookId) {
        return ResponseEntity.ok(readingProgressService.getProgress(principal.getId(), bookId));
    }

    @PutMapping("/libraries/{bookId}/progress")
    public ResponseEntity<ReadingProgressResponse> updateProgress(
            @AuthenticationPrincipal CustomUser principal,
            @PathVariable int bookId,
            @Valid @RequestBody ReadingProgressRequest request) {
        return ResponseEntity.ok(readingProgressService.updateProgress(
                principal.getId(), bookId, request.cfiPosition()));
    }
}
