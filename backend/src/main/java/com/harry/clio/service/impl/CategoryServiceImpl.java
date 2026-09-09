package com.harry.clio.service.impl;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.dto.category.CreateCategoryRequest;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.CategoryMapper;
import com.harry.clio.model.Category;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.repository.CategoryRepository;
import com.harry.clio.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final BookRepository bookRepository;

    @Cacheable(cacheNames = "categories")
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "category", key = "#categoryId")
    public CategoryResponse getCategoryById(int categoryId) {
        return categoryMapper.toResponse(getCategoryOrThrow(categoryId));
    }

    private Category getCategoryOrThrow(int categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại"));
    }

    @Override
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "categories", allEntries = true),
                @CacheEvict(cacheNames = "category", key = "#categoryId"),
                @CacheEvict(cacheNames = "book", allEntries = true),
                @CacheEvict(cacheNames = "books", allEntries = true)
            })
    public CategoryResponse updateCategory(int categoryId, CreateCategoryRequest request) {
        Category category = getCategoryOrThrow(categoryId);
        category.setName(request.name());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "categories", allEntries = true),
                @CacheEvict(cacheNames = "category", key = "#categoryId"),
                @CacheEvict(cacheNames = "book", allEntries = true),
                @CacheEvict(cacheNames = "books", allEntries = true)
            })
    public void deleteCategory(int categoryId) {
        if (bookRepository.existsByCategoriesId(categoryId)) {
            throw new BadRequestException("Không thể xóa do có sách đang trong thể loại");
        }
        Category category = getCategoryOrThrow(categoryId);
        categoryRepository.delete(category);
    }

    @Override
    public Page<CategoryResponse> getAdminCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toResponse);
    }
}
