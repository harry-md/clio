package com.harry.clio.service.impl;

import com.harry.clio.dto.book.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.BookAuthorMapper;
import com.harry.clio.mapper.BookInfoMapper;
import com.harry.clio.mapper.BookMapper;
import com.harry.clio.model.*;
import com.harry.clio.queue.BookQueue;
import com.harry.clio.repository.*;
import com.harry.clio.repository.specification.BookSpecification;
import com.harry.clio.service.BookService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final BookAuthorMapper bookAuthorMapper;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final BookQueue bookProcessingQueue;

    private record BookWithInfo(Book book, BookInfo info) {}

    @Override
    public BookDetailResponse uploadBook(int publisherId, CreateBookMetadataRequest request) {
        BookWithInfo bookWithInfo = transactionTemplate.execute(status -> {
            Set<Category> categories =
                    new HashSet<>(categoryRepository.findAllById(request.categoryIds()));

            List<BookAuthorResponse> authorSnapshots = buildAuthorSnapshot(request.authors());

            Book book = bookRepository.save(bookMapper.toEntity(
                    request,
                    publisherRepository.getReferenceById(publisherId),
                    request.objectKey(),
                    authorSnapshots,
                    categories));

            bookAuthorRepository.saveAll(buildBookAuthors(book, authorSnapshots));

            BookInfo bookInfo = bookInfoRepository.save(BookInfo.builder()
                    .book(book)
                    .isbn(request.isbn())
                    .language(request.language())
                    .description(request.description())
                    .build());
            return new BookWithInfo(book, bookInfo);
        });

        bookProcessingQueue.enqueue(bookWithInfo.book.getId());
        return bookMapper.toDetailResponse(
                bookWithInfo.book, bookInfoMapper.toResponse(bookWithInfo.info));
    }

    private List<BookAuthorResponse> buildAuthorSnapshot(List<BookAuthorResponse> request) {
        Set<Integer> authorIds =
                request.stream().map(BookAuthorResponse::authorId).collect(Collectors.toSet());

        Map<Integer, Author> authors = authorRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Author::getId, author -> author));

        return request.stream()
                .map(authorJson -> {
                    Author author = authors.get(authorJson.authorId());
                    if (author == null) throw new BadRequestException("Tác giả không hợp lệ");
                    return bookAuthorMapper.toResponse(author, authorJson.role());
                })
                .toList();
    }

    private List<BookAuthor> buildBookAuthors(Book book, List<BookAuthorResponse> authorSnapshots) {
        return authorSnapshots.stream()
                .map(snapshot -> BookAuthor.builder()
                        .book(book)
                        .author(authorRepository.getReferenceById(snapshot.authorId()))
                        .role(snapshot.role())
                        .build())
                .toList();
    }

    @Cacheable(cacheNames = "homepage-books", key = """
        'page=' + #pageable.pageNumber +
        '|size=' + #pageable.pageSize +
        '|sort=' +  #pageable.sort.toString()
        """, condition = """
            #pageable.paged &&
            #pageable.pageNumber == 0 &&
            #pageable.pageSize == 12 &&
            #request.hasNoFilters()
            """)
    @Override
    public Page<BookListResponse> getAllBooks(BookFilterRequest request, Pageable pageable) {
        Specification<Book> spec = Specification.where(BookSpecification.hasType(BookType.SYSTEM)
                .and(BookSpecification.hasStatus(BookStatus.COMPLETED))
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

    @Cacheable(cacheNames = "book-details", key = "#bookId")
    @Override
    public BookDetailResponse getBookDetail(int bookId) {
        Book book = bookRepository
                .findWithCategoryById(bookId, BookType.SYSTEM, BookStatus.COMPLETED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách"));

        BookInfo bookInfo = bookInfoRepository
                .findById(book.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sách"));
        return bookMapper.toDetailResponse(book, bookInfoMapper.toResponse(bookInfo));
    }

    @Override
    public int deleteFailedBooks() {
        return bookRepository.deleteFailedBooks(BookStatus.FAILED);
    }

    @Override
    public Page<AdminBookListResponse> getAllAdminBooks(
            BookFilterRequest request, Pageable pageable) {
        Specification<Book> specification = Specification.where(
                        BookSpecification.hasType(BookType.SYSTEM))
                .and(BookSpecification.buildFilter(request));

        Pageable normalizedPageable = applyNullHandling(pageable);

        return bookRepository
                .findAll(specification, normalizedPageable)
                .map(bookMapper::toAdminListResponse);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "homepage-books", allEntries = true),
                @CacheEvict(cacheNames = "book-details", key = "#bookId")
            })
    public void updateBookActive(int bookId, boolean active) {
        Book book = bookRepository
                .findByIdAndType(bookId, BookType.SYSTEM)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách hệ thống"));

        book.setActive(active);
    }
}
