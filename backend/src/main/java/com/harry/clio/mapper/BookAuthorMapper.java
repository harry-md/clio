package com.harry.clio.mapper;

import com.harry.clio.model.Author;
import com.harry.clio.model.BookAuthorResponse;
import com.harry.clio.model.BookAuthorRole;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookAuthorMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorFullname", source = "author.fullName")
    @Mapping(target = "role", source = "role")
    BookAuthorResponse toResponse(Author author, BookAuthorRole role);
}
