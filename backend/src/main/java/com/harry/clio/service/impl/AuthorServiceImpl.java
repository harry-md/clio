package com.harry.clio.service.impl;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.entity.Author;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.AuthorMapper;
import com.harry.clio.repository.AuthorRepository;
import com.harry.clio.service.AuthorService;
import com.harry.clio.service.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final CloudinaryService cloudinaryService;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse getAuthorById(int authorId) {
        return authorMapper.toDto(authorRepository
                .findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả")));
    }

    private void handleDeleteAvatar(String avatarUrl) {
        try {
            cloudinaryService.delete(avatarUrl);
        } catch (RuntimeException ex) {
            log.error("Lỗi khi xóa avatar tác giả {}", ex.getMessage());
        }
    }

    @Override
    public AuthorResponse createAuthor(CreateAuthorRequest request) {
        String avatarUrl = "";
        try {
            if (request.avatarFile() != null && !request.avatarFile().isEmpty()) {
                avatarUrl = cloudinaryService.upload(request.avatarFile());
            }
            final String finalAvatarUrl = avatarUrl;
            return transactionTemplate.execute(status -> {
                Author author = authorMapper.toEntity(request);
                author.setAvatar(finalAvatarUrl);
                return authorMapper.toDto(authorRepository.save(author));
            });
        } catch (RuntimeException ex) {
            if (avatarUrl != null) {
                handleDeleteAvatar(avatarUrl);
            }
            throw ex;
        }
    }

    @Override
    public AuthorResponse updateAuthor(int authorId, UpdateAuthorRequest request) {
        if (request.avatarFile() == null || request.avatarFile().isEmpty()) {
            return transactionTemplate.execute(status -> {
                Author author = authorRepository
                        .findById(authorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"));
                authorMapper.updateEntity(request, author);
                return authorMapper.toDto(authorRepository.save(author));
            });
        }

        String oldAvatarUrl = authorRepository
                .findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"))
                .getAvatar();

        String newAvatarUrl = null;
        try {
            newAvatarUrl = cloudinaryService.upload(request.avatarFile());
            final String finalNewAvatarUrl = newAvatarUrl;

            AuthorResponse response = transactionTemplate.execute(status -> {
                Author author = authorRepository
                        .findById(authorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"));
                authorMapper.updateEntity(request, author);
                author.setAvatar(finalNewAvatarUrl);
                return authorMapper.toDto(author);
            });

            handleDeleteAvatar(oldAvatarUrl);
            return response;
        } catch (RuntimeException ex) {
            if (newAvatarUrl != null) {
                handleDeleteAvatar(newAvatarUrl);
            }
            throw ex;
        }
    }

    @Override
    public void deleteAuthor(int authorId) {
        String avatarUrl = transactionTemplate.execute(status -> {
            Author author = authorRepository
                    .findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"));
            String url = author.getAvatar();
            authorRepository.delete(author);
            return url;
        });
        handleDeleteAvatar(avatarUrl);
    }
}
