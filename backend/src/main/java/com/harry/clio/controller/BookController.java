package com.harry.clio.controller;

import com.harry.clio.dto.book.AdminBookListResponse;
import com.harry.clio.dto.book.BookFilterRequest;
import com.harry.clio.service.BookService;
import com.harry.clio.service.CategoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Controller
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;

    @GetMapping("/books")
    public String list(
            @Valid @ModelAttribute("filter") BookFilterRequest filter,
            BindingResult bindingResult,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable,
            Model model) {
        Page<AdminBookListResponse> books = bindingResult.hasErrors()
                ? Page.empty(pageable)
                : bookService.getAllAdminBooks(filter, pageable);

        model.addAttribute("books", books);
        model.addAttribute("categories", categoryService.getCategories());
        model.addAttribute("pageNumbers", createPageNumbers(books));
        return "html/books";
    }

    private List<Integer> createPageNumbers(Page<?> page) {
        if (page.getTotalPages() == 0) {
            return List.of();
        }

        int startPage = Math.max(0, page.getNumber() - 2);
        int endPage = Math.min(page.getTotalPages() - 1, startPage + 4);

        startPage = Math.max(0, endPage - 4);
        return IntStream.rangeClosed(startPage, endPage).boxed().toList();
    }

    @PostMapping("/books/{bookId}/active")
    public String updateActive(@PathVariable int bookId, @RequestParam boolean active) {
        bookService.updateBookActive(bookId, active);
        return "redirect:/books";
    }
}
