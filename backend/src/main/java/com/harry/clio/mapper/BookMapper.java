package com.harry.clio.mapper;

import com.harry.clio.dto.book.*;
import com.harry.clio.model.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {
    @Mapping(target = "authors", source = "authorList")
    @Mapping(target = "categories", source = "categories")
    @Mapping(target = "publisher", source = "publisher")
    @Mapping(target = "fileUrl", source = "fileUrl")
    @Mapping(target = "status", constant = "QUEUED")
    Book toEntity(
            CreateBookMetadataRequest request,
            Publisher publisher,
            String fileUrl,
            List<BookAuthorResponse> authorList,
            Set<Category> categories);

    BookListResponse toListResponse(Book book);

    @Mapping(target = "bookInfo", source = "bookInfo")
    BookDetailResponse toDetailResponse(Book book, BookInfoResponse bookInfo);
}
