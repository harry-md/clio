package com.harry.clio.service.impl;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.dto.category.CreateCategoryRequest;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.CategoryMapper;
import com.harry.clio.model.Category;
import com.harry.clio.repository.CategoryRepository;
import com.harry.clio.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    private Category getCategoryOrThrow(int categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
    }

    @Override
    public CategoryResponse getCategoryById(int categoryId) {
        return categoryMapper.toResponse(getCategoryOrThrow(categoryId));
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategory(int categoryId, CreateCategoryRequest request) {
        Category category = getCategoryOrThrow(categoryId);
        category.setName(request.name());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(int categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        categoryRepository.delete(category);
    }
}
