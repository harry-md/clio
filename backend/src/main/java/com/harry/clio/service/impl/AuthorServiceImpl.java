package com.harry.clio.service.impl;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.AuthorMapper;
import com.harry.clio.model.Author;
import com.harry.clio.repository.AuthorRepository;
import com.harry.clio.service.AuthorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public AuthorResponse getAuthorById(int authorId) {
        return authorMapper.toResponse(getAuthorOrThrow(authorId));
    }

    private Author getAuthorOrThrow(int authorId) {
        return authorRepository
                .findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"));
    }

    @Override
    public AuthorResponse createAuthor(CreateAuthorRequest request) {
        String avatarUrl = null;
        if (request.avatarFile() != null && !request.avatarFile().isEmpty()) {
            avatarUrl = cloudinaryService.upload(request.avatarFile());
        }

        Author author = authorMapper.toEntity(request);
        if (avatarUrl != null) {
            author.setAvatar(avatarUrl);
        }

        try {
            return authorMapper.toResponse(authorRepository.save(author));
        } catch (RuntimeException ex) {
            if (avatarUrl != null) {
                cloudinaryService.delete(avatarUrl);
            }
            throw ex;
        }
    }

    @Override
    public AuthorResponse updateAuthor(int authorId, UpdateAuthorRequest request) {
        Author author = getAuthorOrThrow(authorId);

        String oldAvatarUrl = author.getAvatar();
        String newAvatarUrl = null;

        authorMapper.updateEntity(request, author);
        if (request.avatarFile() != null && !request.avatarFile().isEmpty()) {
            newAvatarUrl = cloudinaryService.upload(request.avatarFile());
            author.setAvatar(newAvatarUrl);
        }

        AuthorResponse res = null;
        try {
            res = authorMapper.toResponse(authorRepository.save(author));
        } catch (RuntimeException ex) {
            cloudinaryService.delete(newAvatarUrl);
            throw ex;
        }
        if (newAvatarUrl != null) {
            cloudinaryService.delete(oldAvatarUrl);
        }
        return res;
    }

    @Override
    public void deleteAuthor(int authorId) {
        Author author = getAuthorOrThrow(authorId);
        String avatarUrl = author.getAvatar();

        authorRepository.delete(author);

        cloudinaryService.delete(avatarUrl);
    }
}
