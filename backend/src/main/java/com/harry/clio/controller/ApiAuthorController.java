package com.harry.clio.controller;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.service.AuthorService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class ApiAuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> list(@RequestParam(required = false) String kw) {
        return ResponseEntity.ok(authorService.getAllAuthors(kw));
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorResponse> retrieve(@PathVariable int authorId) {
        return ResponseEntity.ok(authorService.getAuthorById(authorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<AuthorResponse> create(
            @Valid @RequestBody CreateAuthorRequest authorRequest) {
        return new ResponseEntity<>(authorService.createAuthor(authorRequest), HttpStatus.CREATED);
    }
}
