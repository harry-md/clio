package com.harry.clio.mapper;

import com.harry.clio.dto.book.BookInfoResponse;
import com.harry.clio.dto.book.CreateBookRequest;
import com.harry.clio.entity.Book;
import com.harry.clio.entity.BookInfo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookInfoMapper {
    @Mapping(target = "bookId", ignore = true)
    @Mapping(target = "book", source = "book")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "isbn", source = "request.isbn")
    @Mapping(target = "language", expression = "java(Language.EN)")
    @Mapping(target = "fileSizeBytes", source = "fileSizeBytes")
    @Mapping(target = "wordCount", expression = "java(0L)")
    BookInfo toEntity(Book book, CreateBookRequest request, Long fileSizeBytes);

    BookInfoResponse toResponse(BookInfo bookInfo);
}
