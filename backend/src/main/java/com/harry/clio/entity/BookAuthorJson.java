package com.harry.clio.entity;

import com.harry.clio.author.BookAuthorRole;

public record BookAuthorJson(Integer id, String authorFullname, BookAuthorRole role) {}
