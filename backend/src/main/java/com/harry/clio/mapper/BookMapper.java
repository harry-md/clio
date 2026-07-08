package com.harry.clio.mapper;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.BookInfoResponse;
import com.harry.clio.dto.book.BookListResponse;
import com.harry.clio.dto.book.CreateBookRequest;
import com.harry.clio.entity.Book;
import com.harry.clio.entity.BookAuthorJson;
import com.harry.clio.entity.Category;
import com.harry.clio.entity.Publisher;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "encryptedFileUrl", ignore = true)
    @Mapping(target = "encryptedContentKey", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    @Mapping(target = "authors", source = "authors")
    @Mapping(target = "categories", source = "categories")
    @Mapping(target = "publisher", source = "publisher")
    @Mapping(target = "fileUrl", source = "originFileUrl")
    Book toEntity(
            CreateBookRequest request,
            Publisher publisher,
            String originFileUrl,
            List<BookAuthorJson> authors,
            Set<Category> categories);

    BookListResponse toListResponse(Book book);

    @Mapping(target = "bookInfo", source = "bookInfo")
    BookDetailResponse toDetailResponse(Book book, BookInfoResponse bookInfo);

    default List<BookAuthorJson> toAuthorSnapshots(
            List<BookAuthorJson> requestAuthors, List<AuthorResponse> authorResponses) {
        Map<Integer, AuthorResponse> authorById = authorResponses.stream()
                .collect(Collectors.toMap(AuthorResponse::id, author -> author));

        return requestAuthors.stream()
                .map(requestAuthor -> {
                    AuthorResponse author = authorById.get(requestAuthor.authorId());
                    return new BookAuthorJson(author.id(), author.fullName(), requestAuthor.role());
                })
                .toList();
    }
}
