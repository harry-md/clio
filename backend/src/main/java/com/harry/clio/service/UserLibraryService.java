package com.harry.clio.service;

import com.harry.clio.dto.library.LibraryResponse;

public interface UserLibraryService {
    LibraryResponse addToLibrary(Integer userId, Integer bookId);
}
