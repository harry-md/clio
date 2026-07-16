package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/publishers")
public class ApiPublisherController {
    private final PublisherService publisherService;

    @GetMapping("/current-publisher")
    public ResponseEntity<PublisherDto> retrieve(@AuthenticationPrincipal CustomUser principal) {
        return ResponseEntity.ok(publisherService.getPublisherByUserId(principal.getId()));
    }

    @PreAuthorize("hasRole('PUBLISHER')")
    @PatchMapping("/current-publisher")
    public ResponseEntity<PublisherDto> update(
            @AuthenticationPrincipal CustomUser principal, @RequestBody PublisherDto publisherDto) {
        return ResponseEntity.ok(publisherService.updatePublisher(principal.getId(), publisherDto));
    }
}
