package com.harry.clio.repository.specification;
import org.hibernate.query.criteria.HibernateCriteriaBuilder
import com.harry.clio.dto.book.BookFilterRequest;
import com.harry.clio.entity.Book;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {
    public static Specification<Book> buildFilter(BookFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.title() != null && !request.title().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        String.format("%%%s%%", request.title().toLowerCase())));
            }
            if (request.authorFullname() != null && !request.authorFullname().isBlank()) {
                HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
                Expression<String> authorsText = hcb.cast((JpaExpression<Object>) root.get("authors"), String.class);
                predicates.add(cb.like(
                        cb.lower(authorsText),
                        String.format("%%%s%%", request.authorFullname().toLowerCase())));
            }
            if (request.fromPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.fromPrice()));
            }
            if (request.toPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.toPrice()));
            }
            if (request.fromRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.fromRating()));
            }
            if (request.toRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), request.toRating()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
