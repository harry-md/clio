package com.harry.clio.service.impl;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.AuthorMapper;
import com.harry.clio.model.Author;
import com.harry.clio.repository.AuthorRepository;
import com.harry.clio.repository.BookRepository;
import com.harry.clio.repository.specification.AuthorSpecification;
import com.harry.clio.service.AuthorService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookRepository bookRepository;

    @Override
    @Cacheable(cacheNames = "authors", condition = "#kw == null")
    public List<AuthorResponse> getAllAuthors(String kw) {
        Specification<Author> spec = AuthorSpecification.hasKw(kw);

        return authorRepository.findAll(spec).stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Override
    public Page<AuthorResponse> getAdminAuthors(String keyword, Pageable pageable) {
        return authorRepository
                .findAll(AuthorSpecification.hasKw(keyword), pageable)
                .map(authorMapper::toResponse);
    }

    @Override
    @Cacheable(cacheNames = "author", key = "#authorId")
    public AuthorResponse getAuthorById(int authorId) {
        return authorMapper.toResponse(getAuthorOrThrow(authorId));
    }

    @Override
    @CacheEvict(cacheNames = "authors", allEntries = true)
    public AuthorResponse createAuthor(CreateAuthorRequest request) {
        Author author = authorMapper.toEntity(request);
        author.setFullName(request.fullName().strip());

        return authorMapper.toResponse(authorRepository.save(author));
    }

    private Author getAuthorOrThrow(int authorId) {
        return authorRepository
                .findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"));
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "authors", allEntries = true),
                @CacheEvict(cacheNames = "author", key = "#authorId"),
                @CacheEvict(cacheNames = "books", allEntries = true),
                @CacheEvict(cacheNames = "book-detail", allEntries = true)
            })
    public AuthorResponse updateAuthor(int authorId, UpdateAuthorRequest request) {
        Author author = getAuthorOrThrow(authorId);
        String fullName = request.fullName();
        boolean needUpdateFullname = !author.getFullName().equals(fullName);

        author.setFullName(fullName);
        author.setBiography(request.biography());
        author.setVerified(request.verified());

        authorRepository.saveAndFlush(author);

        if (needUpdateFullname) {
            bookRepository.updateAuthorsByAuthorId(authorId);
        }
        return authorMapper.toResponse(author);
    }

    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "authors", allEntries = true),
                @CacheEvict(cacheNames = "author", key = "#authorId")
            })
    public void deleteAuthor(int authorId) {
        authorRepository.delete(getAuthorOrThrow(authorId));
    }
}
