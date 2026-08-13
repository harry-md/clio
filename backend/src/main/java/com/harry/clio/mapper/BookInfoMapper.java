package com.harry.clio.mapper;

import com.harry.clio.dto.book.BookInfoResponse;
import com.harry.clio.dto.book.CreateBookMetadataRequest;
import com.harry.clio.entity.Book;
import com.harry.clio.entity.BookInfo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookInfoMapper {
    @Mapping(target = "book", source = "book")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "isbn", source = "request.isbn")
    @Mapping(target = "fileSize", source = "fileSize")
    @Mapping(target = "wordCount", expression = "java(0L)")
    BookInfo toEntity(Book book, CreateBookMetadataRequest request, Long fileSize);

    BookInfoResponse toResponse(BookInfo bookInfo);
}
