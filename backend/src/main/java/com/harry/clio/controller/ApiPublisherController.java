package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.dto.stats.PublisherDashboardResponse;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.service.PublisherService;
import com.harry.clio.service.StatService;

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
    private final StatService statsService;

    @GetMapping("/current-publisher")
    public ResponseEntity<PublisherDto> retrieve(@AuthenticationPrincipal CustomUser principal) {
        return ResponseEntity.ok(publisherService.getPublisherByUserId(principal.getId()));
    }

    @GetMapping("/current-publisher/dashboard")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<PublisherDashboardResponse> getDashboard(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal CustomUser principal) {
        if (year < 1 || month < 1 || month > 12) {
            throw new BadRequestException("Tháng năm không hợp lệ");
        }
        return ResponseEntity.ok(
                statsService.getPublisherDashboard(principal.getId(), year, month));
    }
}
