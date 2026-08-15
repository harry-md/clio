package com.harry.clio.mapper;

import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.model.UserLibrary;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserLibraryMapper {
    @Mapping(target = "id", source = "userLibrary.id")
    @Mapping(target = "type", source = "userLibrary.type")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "thumbnail", source = "book.thumbnail")
    @Mapping(target = "authors", source = "book.authors")
    LibraryResponse toResponse(UserLibrary userLibrary);
}
