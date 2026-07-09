package com.harry.clio.controller;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.service.AuthorService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorResponse> retrieve(@PathVariable int authorId) {
        return ResponseEntity.ok(authorService.getAuthorById(authorId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthorResponse> create(
            @Valid @ModelAttribute CreateAuthorRequest authorRequest) {
        return new ResponseEntity<>(authorService.createAuthor(authorRequest), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{authorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthorResponse> update(
            @PathVariable int authorId, @Valid @ModelAttribute UpdateAuthorRequest authorRequest) {
        return new ResponseEntity<>(
                authorService.updateAuthor(authorId, authorRequest), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{authorId}")
    public ResponseEntity<Void> delete(@PathVariable int authorId) {
        authorService.deleteAuthor(authorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
