package com.harry.clio.book;

import com.harry.clio.author.BookAuthorRole;

public record BookAuthorJson(Integer id, String authorFullname, BookAuthorRole role) {}
