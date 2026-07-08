package com.harry.clio.service.impl;

import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.CreateBookRequest;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.mapper.BookInfoMapper;
import com.harry.clio.mapper.BookMapper;
import com.harry.clio.repository.*;
import com.harry.clio.service.BookProcessingQueue;
import com.harry.clio.service.BookService;
import com.harry.clio.service.R2Service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final BookMapper bookMapper;
    private final BookInfoMapper bookInfoMapper;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final R2Service r2Service;
    private final BookProcessingQueue bookProcessingQueue;

    @Override
    public BookDetailResponse uploadBook(int publisherId, CreateBookRequest request) {
        if (categoryRepository.countByIdIn(request.categoryIds())
                != request.categoryIds().size()) {
            throw new BadRequestException("Danh mục sách không hợp lệ");
        }

        return null;
    }
}
