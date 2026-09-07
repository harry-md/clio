package com.harry.clio.controller;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.service.AuthorService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

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
        Page<AuthorResponse> authors = authorService.getAdminAuthors(keyword, authorPage(page));

        if (authors.isEmpty() && authors.getNumber() > 0) {
            authors = authorService.getAdminAuthors(
                    keyword, authorPage(Math.max(0, authors.getTotalPages() - 1)));
        }

        model.addAttribute("authors", authors);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageNumbers", createPageNumbers(authors));
        return "authors";
    }

    @GetMapping("/new")
    public String createView(Model model) {
        model.addAttribute("form", new CreateAuthorRequest("", ""));
        return formView(model, null);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") CreateAuthorRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return formView(model, null);
        }

        authorService.createAuthor(form);
        redirectAttributes.addFlashAttribute("success", "Đã thêm tác giả.");
        return "redirect:/authors";
    }

    @GetMapping("/{authorId}/edit")
    public String editView(@PathVariable int authorId, Model model) {
        AuthorResponse author = authorService.getAuthorById(authorId);
        model.addAttribute(
                "form",
                new UpdateAuthorRequest(author.fullName(), author.biography(), author.verified()));
        return formView(model, authorId);
    }

    @PostMapping("/{authorId}")
    public String update(
            @PathVariable int authorId,
            @Valid @ModelAttribute("form") UpdateAuthorRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return formView(model, authorId);
        }

        authorService.updateAuthor(authorId, form);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật tác giả.");
        return "redirect:/authors";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String notFound(
            ResourceNotFoundException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", exception.getMessage());
        return "redirect:/authors";
    }

    private String formView(Model model, Integer authorId) {
        model.addAttribute("isEdit", authorId != null);
        model.addAttribute("authorId", authorId);
        return "author-form";
    }

    private PageRequest authorPage(int page) {
        return PageRequest.of(Math.max(0, page), 10, Sort.by(Sort.Direction.DESC, "id"));
    }
}
