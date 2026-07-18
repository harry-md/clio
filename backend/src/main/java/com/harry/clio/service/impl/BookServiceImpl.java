package com.harry.clio.service.impl;

import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.BookFilterRequest;
import com.harry.clio.dto.book.BookListResponse;
import com.harry.clio.dto.book.CreateBookMetadataRequest;
import com.harry.clio.entity.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.BookInfoMapper;
import com.harry.clio.mapper.BookMapper;
import com.harry.clio.repository.*;
import com.harry.clio.repository.specification.BookSpecification;
import com.harry.clio.service.BookProcessingQueue;
import com.harry.clio.service.BookService;
import com.harry.clio.service.R2Service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

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
    private final BookProcessingQueue bookProcessingQueue;

    private record CreatedBook(Integer bookId, BookDetailResponse response) {}

    @Override
    public BookDetailResponse uploadBook(
            int publisherId, CreateBookMetadataRequest request, MultipartFile file) {
        if (categoryRepository.countByIdIn(request.categoryIds())
                != request.categoryIds().size()) {
            throw new BadRequestException("Danh mục không hợp lệ");
        }

        String fileUrl = null;
        CreatedBook savedBook;
        try {
            fileUrl = r2Service.uploadOriginEbook(file);
            final String finalFileUrl = fileUrl;

            savedBook = transactionTemplate.execute(status -> {
                Set<Category> categories =
                        new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
                List<BookAuthorJson> authorSnapshots = buildAuthorSnapshot(request.authors());

                Book book = bookRepository.save(bookMapper.toEntity(
                        request,
                        publisherRepository.getReferenceById(publisherId),
                        finalFileUrl,
                        authorSnapshots,
                        categories));

                bookAuthorRepository.saveAll(buildBookAuthors(book, authorSnapshots));

                BookInfo bookInfo = bookInfoRepository.save(BookInfo.builder()
                        .book(book)
                        .isbn(request.isbn())
                        .description(request.description())
                        .fileSize(file.getSize())
                        .build());

                return new CreatedBook(
                        book.getId(),
                        bookMapper.toDetailResponse(book, bookInfoMapper.toResponse(bookInfo)));
            });
        } catch (RuntimeException ex) {
            if (fileUrl != null) r2Service.delete(fileUrl);
            throw ex;
        }
        bookProcessingQueue.enqueue(savedBook.bookId());
        return savedBook.response();
    }

    private List<BookAuthorJson> buildAuthorSnapshot(List<BookAuthorJson> request) {
        Set<Integer> authorIds =
                request.stream().map(BookAuthorJson::authorId).collect(Collectors.toSet());
        Map<Integer, Author> authors = authorRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Author::getId, author -> author));
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

    @Override
    public Page<BookListResponse> getAllBooks(BookFilterRequest request, Pageable pageable) {
        Specification<Book> spec = Specification.where(BookSpecification.hasType(BookType.SYSTEM)
                .and(BookSpecification.isActive(true).and(BookSpecification.buildFilter(request))));
        Pageable normalizedPageable = applyNullHandling(pageable);
        return bookRepository.findAll(spec, normalizedPageable).map(bookMapper::toListResponse);
    }

    private Pageable applyNullHandling(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    if ("rating".equals(order.getProperty())) {
                        return order.nullsLast();
                    }
                    return order;
                })
                .toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    @Override
    public BookDetailResponse getBookDetail(Integer bookId) {
        Book book = bookRepository
                .findWithDetailById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));
        BookInfo bookInfo = bookInfoRepository
                .findById(book.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sách"));
        return bookMapper.toDetailResponse(book, bookInfoMapper.toResponse(bookInfo));
    }
}
