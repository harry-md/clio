package com.harry.clio.controller;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.dto.category.CreateCategoryRequest;
import com.harry.clio.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok().body(categoryService.getCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> retrieve(@PathVariable int categoryId) {
        return ResponseEntity.ok().body(categoryService.getCategoryById(categoryId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<CategoryResponse> create(@RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok().body(categoryService.createCategory(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable int categoryId, @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok().body(categoryService.updateCategory(categoryId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable int categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
