package com.harry.clio.dto.author;

import java.io.Serializable;

public record AuthorResponse(
        Integer id, String fullName, String biography, String avatar, boolean verified)
        implements Serializable {}
