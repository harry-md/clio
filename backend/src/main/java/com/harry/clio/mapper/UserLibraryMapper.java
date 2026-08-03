package com.harry.clio.mapper;

import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.entity.UserLibrary;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserLibraryMapper {
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "type", source = "entity.type")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "thumbnail", source = "book.thumbnail")
    @Mapping(target = "authors", source = "book.authors")
    LibraryResponse toResponse(UserLibrary entity);
}
