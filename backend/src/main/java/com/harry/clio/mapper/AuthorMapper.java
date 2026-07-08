package com.harry.clio.mapper;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.entity.Author;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "verified", ignore = true)
    Author toEntity(CreateAuthorRequest dto);

    AuthorResponse toDto(Author entity);

    @Mapping(target = "avatar", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Author updateEntity(UpdateAuthorRequest dto, @MappingTarget Author entity);
}
