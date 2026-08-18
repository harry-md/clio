package com.harry.clio.repository.specification;

import com.harry.clio.dto.user.UserFilterRequest;
import com.harry.clio.model.User;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UserSpecification {
    private UserSpecification() {}

    public static Specification<User> buildFilter(UserFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.username() != null && !request.username().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("username")), contains(request.username())));
            }
            if (request.firstName() != null && !request.firstName().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("firstName")), contains(request.firstName())));
            }
            if (request.lastName() != null && !request.lastName().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("lastName")), contains(request.lastName())));
            }
            if (request.email() != null && !request.email().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), contains(request.email())));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
