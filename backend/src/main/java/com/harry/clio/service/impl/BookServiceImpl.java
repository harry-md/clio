package com.harry.clio.service.impl;

import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.CreateBookRequest;
import com.harry.clio.entity.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.mapper.BookInfoMapper;
import com.harry.clio.mapper.BookMapper;
import com.harry.clio.repository.*;
import com.harry.clio.service.BookService;
import com.harry.clio.service.R2Service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final TransactionTemplate transactionTemplate;
    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final BookMapper bookMapper;
    private final BookInfoMapper bookInfoMapper;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final R2Service r2Service;

    @Override
    public BookDetailResponse uploadBook(int publisherId, CreateBookRequest request) {
        if (categoryRepository.countByIdIn(request.categoryIds())
                != request.categoryIds().size()) {
            throw new BadRequestException("Danh mục không hợp lệ");
        }

        String originFileUrl = null;
        try {
            originFileUrl = r2Service.uploadOriginEbook(request.file());
            final String finalOriginFileUrl = originFileUrl;

            return transactionTemplate.execute(status -> {
                Set<Category> categories =
                        new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
                List<BookAuthorJson> authorSnapshots = buildAuthorSnapshot(request.authors());

                Book book = bookRepository.save(bookMapper.toEntity(
                        request,
                        publisherRepository.getReferenceById(publisherId),
                        finalOriginFileUrl,
                        authorSnapshots,
                        categories));

                bookAuthorRepository.saveAll(buildBookAuthors(book, authorSnapshots));

                BookInfo bookInfo = bookInfoRepository.save(BookInfo.builder()
                        .book(book)
                        .isbn(request.isbn())
                        .description(request.description())
                        .fileSizeBytes(request.file().getSize())
                        .build());
                return bookMapper.toDetailResponse(book, bookInfoMapper.toResponse(bookInfo));
            });
        } catch (RuntimeException ex) {
            if (originFileUrl != null) r2Service.delete(originFileUrl);
            throw ex;
        }
    }

    private List<BookAuthorJson> buildAuthorSnapshot(List<BookAuthorJson> request) {
        Set<Integer> authorIds =
                request.stream().map(BookAuthorJson::authorId).collect(Collectors.toSet());
        Map<Integer, Author> authors = authorRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(author -> author.getId(), author -> author));
        return request.stream()
                .map(authorJson -> {
                    Author author = authors.get(authorJson.authorId());
                    if (author == null) throw new BadRequestException("Tác giả không hợp lệ");
                    return new BookAuthorJson(
                            author.getId(), author.getFullName(), authorJson.role());
                })
                .toList();
    }

    private List<BookAuthor> buildBookAuthors(Book book, List<BookAuthorJson> authorSnapshots) {
        return authorSnapshots.stream()
                .map(snapshot -> BookAuthor.builder()
                        .book(book)
                        .author(authorRepository.getReferenceById(snapshot.authorId()))
                        .role(snapshot.role())
                        .build())
                .toList();
    }
}
