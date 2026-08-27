package com.harry.clio.repository.specification;

import com.harry.clio.dto.user.UserFilterRequest;
import com.harry.clio.model.User;

import jakarta.persistence.criteria.Expression;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class UserSpecification {
    public static Specification<User> buildFilter(UserFilterRequest request) {
        return (root, query, cb) -> {
            if (request.keyword() == null || request.keyword().isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + request.keyword().trim().toLowerCase(Locale.ROOT) + "%";

            Expression<String> fullName = cb.lower(cb.concat(
                    cb.concat(root.<String>get("firstName"), " "), root.<String>get("lastName")));

            return cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(fullName, pattern));
        };
    }
}
