package com.harry.clio.mapper;

import com.harry.clio.dto.category.CategoryResponse;
import com.harry.clio.dto.category.CreateCategoryRequest;
import com.harry.clio.entity.Category;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CreateCategoryRequest dto);

    CategoryResponse toDto(Category entity);
}
