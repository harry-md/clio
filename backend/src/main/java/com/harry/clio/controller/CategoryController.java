package com.harry.clio.controller;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.dto.category.CreateCategoryRequest;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.service.CategoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

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
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<CategoryResponse> categories = categoryService.getAdminCategories(
                PageRequest.of(Math.max(0, page), 10, Sort.by(Sort.Direction.DESC, "id")));

        if (categories.isEmpty() && categories.getNumber() > 0) {
            categories = categoryService.getAdminCategories(PageRequest.of(
                    Math.max(0, categories.getTotalPages() - 1),
                    10,
                    Sort.by(Sort.Direction.DESC, "id")));
        }

        model.addAttribute("categories", categories);
        model.addAttribute("pageNumbers", createPageNumbers(categories));
        return "categories";
    }

    private String formView(Model model, Integer categoryId) {
        model.addAttribute("isEdit", categoryId != null);
        model.addAttribute("categoryId", categoryId);
        return "category-form";
    }

    @GetMapping("/new")
    public String createView(Model model) {
        model.addAttribute("form", new CreateCategoryRequest(""));
        return formView(model, null);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") CreateCategoryRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return formView(model, null);
        }

        categoryService.createCategory(form);
        redirectAttributes.addFlashAttribute("success", "Đã thêm thể loại.");
        return "redirect:/categories";
    }

    @GetMapping("/{categoryId}/edit")
    public String editView(@PathVariable int categoryId, Model model) {
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        model.addAttribute("form", new CreateCategoryRequest(category.name()));
        return formView(model, categoryId);
    }

    @PostMapping("/{categoryId}")
    public String update(
            @PathVariable int categoryId,
            @Valid @ModelAttribute("form") CreateCategoryRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return formView(model, categoryId);
        }

        categoryService.updateCategory(categoryId, form);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật thể loại.");
        return "redirect:/categories";
    }

    @PostMapping("/{categoryId}/delete")
    public String delete(@PathVariable int categoryId, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(categoryId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa thể loại.");
        return "redirect:/categories";
    }

    @ExceptionHandler({BadRequestException.class, ResourceNotFoundException.class})
    public String showError(RuntimeException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", exception.getMessage());
        return "redirect:/categories";
    }
}
