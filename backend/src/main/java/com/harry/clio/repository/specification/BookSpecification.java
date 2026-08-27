package com.harry.clio.repository.specification;

import com.harry.clio.dto.book.BookFilterRequest;
import com.harry.clio.model.Book;
import com.harry.clio.model.BookStatus;
import com.harry.clio.model.BookType;
import com.harry.clio.model.Category;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
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
            if (request.keyword() != null && !request.keyword().isBlank()) {
                Expression<String> authorsText = ((HibernateCriteriaBuilder) cb)
                        .cast((JpaExpression<Object>) root.get("authors"), String.class);

                String keyword = String.format("%%%s%%", request.keyword().toLowerCase());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(authorsText), keyword)));
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
            if (request.categoryId() != null) {
                Join<Book, Category> categoryJoin = root.join("categories");
                predicates.add(cb.equal(categoryJoin.get("id"), request.categoryId()));
            }

            if (request.authorId() != null) {
                HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
                var authorExists = hcb.jsonExists(
                                root.get("authors"), "$[*] ? (@.authorId == $authorId)")
                        .passing("authorId", cb.literal(request.authorId()));
                predicates.add(cb.isTrue(authorExists));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Book> hasType(BookType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Book> hasStatus(BookStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Book> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
