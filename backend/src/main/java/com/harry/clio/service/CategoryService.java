package com.harry.clio.service;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.dto.category.CreateCategoryRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategories();

    CategoryResponse getCategoryById(int categoryId);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(int categoryId, CreateCategoryRequest request);

    void deleteCategory(int categoryId);

    Page<CategoryResponse> getAdminCategories(Pageable pageable);
}
