package com.harry.clio.service;

import com.harry.clio.dto.author.AuthorResponse;
import com.harry.clio.dto.author.CreateAuthorRequest;
import com.harry.clio.dto.author.UpdateAuthorRequest;

public interface AuthorService {
    AuthorResponse getAuthorById(int authorId);

    AuthorResponse createAuthor(CreateAuthorRequest request);

    AuthorResponse updateAuthor(int authorId, UpdateAuthorRequest request);

    void deleteAuthor(int authorId);
}
