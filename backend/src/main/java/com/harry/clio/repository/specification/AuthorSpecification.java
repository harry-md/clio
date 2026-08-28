package com.harry.clio.repository.specification;

import com.harry.clio.model.Author;

import org.springframework.data.jpa.domain.Specification;

public class AuthorSpecification {
    public static Specification<Author> hasKw(String kw) {
        return (root, query, cb) -> {
            if (kw == null || kw.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("fullName")), String.format("%%%s%%", kw.toLowerCase()));
        };
    }
}
