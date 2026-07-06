package com.harry.clio.dto;

import com.harry.clio.entity.BookType;

import java.math.BigDecimal;

public record CreateBookRequest(String title, BigDecimal price, BookType type) {}
