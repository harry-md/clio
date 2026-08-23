package com.harry.clio.dto.stats;

public record TopSellingBookResponse(
        Integer bookId, String title, String thumbnail, Long salesCount) {}
