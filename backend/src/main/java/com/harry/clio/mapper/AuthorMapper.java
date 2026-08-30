package com.harry.clio.mapper;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.model.Author;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {
    Author toEntity(CreateAuthorRequest dto);

    AuthorResponse toResponse(Author author);
}
