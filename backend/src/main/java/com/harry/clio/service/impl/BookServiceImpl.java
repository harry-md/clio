package com.harry.clio.service.impl;

import com.harry.clio.dto.book.*;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.infra.BookQueue;
import com.harry.clio.mapper.BookAuthorMapper;
import com.harry.clio.mapper.BookInfoMapper;
import com.harry.clio.mapper.BookMapper;
import com.harry.clio.model.*;
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

@Service
@RequiredArgsConstructor
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
    private final BookQueue bookQueue;

    @Override
    public void uploadBook(int publisherId, CreateBookMetadataRequest request) {
        int bookId = transactionTemplate.execute(status -> {
            Set<Category> categories =
                    new HashSet<>(categoryRepository.findAllById(request.categoryIds()));

            List<BookAuthorInfo> authorSnapshots = buildAuthorSnapshot(request.authors());

            Book book = bookRepository.save(bookMapper.toEntity(
                    request,
                    publisherRepository.getReferenceById(publisherId),
                    request.objectKey(),
                    authorSnapshots,
                    categories));

            bookAuthorRepository.saveAll(buildBookAuthors(book, authorSnapshots));

            bookInfoRepository.save(BookInfo.builder()
                    .book(book)
                    .isbn(request.isbn())
                    .language(request.language())
                    .description(request.description())
                    .build());
            return book.getId();
        });

        bookQueue.enqueue(bookId);
    }

    private List<BookAuthorInfo> buildAuthorSnapshot(List<BookAuthorInfo> request) {
        Set<Integer> authorIds =
                request.stream().map(BookAuthorInfo::authorId).collect(Collectors.toSet());

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

    private List<BookAuthor> buildBookAuthors(Book book, List<BookAuthorInfo> authorSnapshots) {
        return authorSnapshots.stream()
                .map(snapshot -> BookAuthor.builder()
                        .book(book)
                        .author(authorRepository.getReferenceById(snapshot.authorId()))
                        .role(snapshot.role())
                        .build())
                .toList();
    }

    @Cacheable(cacheNames = "books", key = """
        'page:'+#pageable.pageNumber +
        ':sort:'+#pageable.sort.toString()
        """, condition = """
            #pageable.paged &&
            #pageable.pageNumber >= 0 &&
            #pageable.pageNumber <= 3 &&
            #pageable.pageSize == 12 &&
            #request.hasNoFilters()
            """)
    @Override
    public Page<BookListResponse> getAllBooks(BookFilterRequest request, Pageable pageable) {
        Specification<Book> spec = Specification.where(BookSpecification.hasType(BookType.SYSTEM)
                .and(BookSpecification.hasStatus(BookStatus.COMPLETED))
                .and(BookSpecification.isActive(true))
                .and(BookSpecification.buildFilter(request)));

        Pageable normalizedPageable = handleRatingNull(pageable);
        return bookRepository.findAll(spec, normalizedPageable).map(bookMapper::toListResponse);
    }

    private Pageable handleRatingNull(Pageable pageable) {
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

    @Cacheable(cacheNames = "book", key = "#bookId")
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
    @Transactional
    public int deleteFailedBooks() {
        List<Integer> bookIds = bookRepository.findIdsByStatus(BookStatus.FAILED);
        if (bookIds.isEmpty()) {
            return 0;
        }

        bookInfoRepository.deleteByBookIds(bookIds);
        bookAuthorRepository.deleteByBookIds(bookIds);
        bookRepository.deleteBookCategoryByBookIds(bookIds);

        return bookRepository.deleteByBookIds(bookIds);
    }

    @Override
    public Page<AdminBookListResponse> getAllAdminBooks(
            BookFilterRequest request, Pageable pageable) {
        Specification<Book> specification = Specification.where(
                        BookSpecification.hasType(BookType.SYSTEM))
                .and(BookSpecification.buildFilter(request));

        Pageable normalizedPageable = handleRatingNull(pageable);

        return bookRepository
                .findAll(specification, normalizedPageable)
                .map(bookMapper::toAdminListResponse);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "books", allEntries = true),
                @CacheEvict(cacheNames = "book", key = "#bookId")
            })
    public void updateBookActive(int bookId, boolean active) {
        Book book = bookRepository
                .findByIdAndType(bookId, BookType.SYSTEM)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách hệ thống"));
        book.setActive(active);
    }
}
