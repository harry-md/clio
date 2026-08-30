package com.harry.clio.repository.specification;

import com.harry.clio.dto.book.BookFilterRequest;
import com.harry.clio.model.Book;
import com.harry.clio.model.BookStatus;
import com.harry.clio.model.BookType;
import com.harry.clio.model.Category;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaJsonExistsExpression;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class BookSpecification {
    public static Specification<Book> buildFilter(BookFilterRequest request) {
        return Specification.allOf(
                searchKeyword(request.keyword()),
                fromPrice(request.fromPrice()),
                toPrice(request.toPrice()),
                fromRating(request.fromRating()),
                toRating(request.toRating()),
                hasCategoryId(request.categoryId()),
                hasAuthorId(request.authorId()));
    }

    public static Specification<Book> searchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.unrestricted();
        }

        return (root, query, cb) -> {
            Expression<String> authorText = ((HibernateCriteriaBuilder) cb)
                    .cast((JpaExpression<Object>) root.get("authors"), String.class);

            String pattern = String.format("%%%s%%", keyword.toLowerCase());
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(authorText), pattern));
        };
    }

    public static Specification<Book> fromPrice(BigDecimal fromPrice) {
        if (fromPrice == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), fromPrice);
    }

    public static Specification<Book> toPrice(BigDecimal toPrice) {
        if (toPrice == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), toPrice);
    }

    public static Specification<Book> fromRating(Integer fromRating) {
        if (fromRating == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rating"), fromRating);
    }

    public static Specification<Book> toRating(Integer toRating) {
        if (toRating == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("rating"), toRating);
    }

    public static Specification<Book> hasCategoryId(Integer categoryId) {
        if (categoryId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            Join<Book, Category> categoryJoin = root.join("categories");
            return cb.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Book> hasAuthorId(Integer authorId) {
        if (authorId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
            JpaJsonExistsExpression expression = hcb.jsonExists(
                            root.get("authors"), "$[*] ? (@.authorId == $authorId)")
                    .passing("authorId", cb.literal(authorId));
            return cb.isTrue(expression);
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
