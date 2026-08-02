package com.harry.clio.mapper;

import com.harry.clio.entity.Author;
import com.harry.clio.entity.BookAuthorResponse;
import com.harry.clio.entity.BookAuthorRole;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookAuthorMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorFullname", source = "author.fullName")
    @Mapping(target = "role", source = "role")
    BookAuthorResponse toResponse(Author author, BookAuthorRole role);
}
