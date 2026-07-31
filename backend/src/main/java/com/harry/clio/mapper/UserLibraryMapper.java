package com.harry.clio.mapper;

import com.harry.clio.dto.library.LibraryResponse;
import com.harry.clio.entity.Book;
import com.harry.clio.entity.UserLibrary;
import com.harry.clio.entity.UserLibraryType;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserLibraryMapper {
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "cfiPosition", source = "cfiPosition")
    @Mapping(target = "book.id", source = "book.id")
    @Mapping(target = "book.title", source = "book.title")
    @Mapping(target = "book.thumbnail", source = "book.thumbnail")
    @Mapping(target = "book.authors", source = "book.authors")
    LibraryResponse toResponse(
            UserLibrary entity, UserLibraryType type, String cfiPosition, Book book);
}
