package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.service.UserLibraryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiUserLibraryController {
    private final UserLibraryService userLibraryService;

    @PostMapping("/books/{bookId}/library")
    public ResponseEntity<LibraryResponse> addToLibrary(
            @AuthenticationPrincipal CustomUser principal, @PathVariable Integer bookId) {
        return ResponseEntity.ok(userLibraryService.addToLibrary(principal.getId(), bookId));
    }
}
