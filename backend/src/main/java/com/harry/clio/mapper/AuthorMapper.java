package com.harry.clio.mapper;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.entity.Author;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {
    Author toEntity(CreateAuthorRequest dto);

    AuthorResponse toDto(Author entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateAuthorRequest dto, @MappingTarget Author entity);
}
