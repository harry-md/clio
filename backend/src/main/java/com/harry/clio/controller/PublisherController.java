package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/publishers")
public class PublisherController {
    private final PublisherService publisherService;

    @GetMapping("/current-publisher")
    public ResponseEntity<PublisherDto> selfRetrieve(
            @AuthenticationPrincipal CustomUser principal) {
        return ResponseEntity.ok(publisherService.getPublisherByUserId(principal.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{publisherId}")
    public ResponseEntity<PublisherDto> retrieve(@PathVariable int publisherId) {
        return ResponseEntity.ok(publisherService.getPublisherByUserId(publisherId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    public ResponseEntity<List<PublisherDto>> list() {
        return ResponseEntity.ok(publisherService.getAllPublishers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<PublisherDto> create(@RequestBody PublisherDto publisherDto) {
        return new ResponseEntity<>(
                publisherService.createPublisher(publisherDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('PUBLISHER')")
    @PatchMapping("/current-publisher")
    public ResponseEntity<PublisherDto> selfUpdate(
            @AuthenticationPrincipal CustomUser principal, @RequestBody PublisherDto publisherDto) {
        return ResponseEntity.ok(publisherService.updatePublisher(principal.getId(), publisherDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{publisherId}")
    public ResponseEntity<PublisherDto> update(
            @PathVariable int publisherId, @RequestBody PublisherDto publisherDto) {
        return ResponseEntity.ok(publisherService.updatePublisher(publisherId, publisherDto));
    }
}
