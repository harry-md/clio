package com.harry.clio.controller;

import com.harry.clio.dto.review.AdminReviewResponse;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    private List<Integer> createPageNumbers(Page<?> page) {
        if (page.getTotalPages() == 0) {
            return List.of();
        }

        int startPage = Math.max(0, page.getNumber() - 2);
        int endPage = Math.min(page.getTotalPages() - 1, startPage + 4);
        startPage = Math.max(0, endPage - 4);
        return IntStream.rangeClosed(startPage, endPage).boxed().toList();
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        keyword = keyword.strip();

        Page<AdminReviewResponse> reviews =
                reviewService.adminGetAllReviews(keyword, PageRequest.of(Math.max(0, page), 10));

        if (reviews.isEmpty() && reviews.getNumber() > 0) {
            reviews = reviewService.adminGetAllReviews(
                    keyword, PageRequest.of(Math.max(0, reviews.getTotalPages() - 1), 10));
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageNumbers", createPageNumbers(reviews));
        return "reviews";
    }

    @PostMapping("/{reviewId}/delete")
    public String delete(
            @PathVariable int reviewId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        try {
            reviewService.adminDeleteReview(reviewId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá.");
        } catch (ResourceNotFoundException | BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("page", Math.max(0, page));
        return "redirect:/reviews";
    }
}
