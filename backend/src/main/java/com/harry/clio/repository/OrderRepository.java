package com.harry.clio.repository;

import com.harry.clio.entity.Order;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT o
        FROM Order o
        WHERE o.id = :id
        """)
    Optional<Order> findByIdForUpdate(@Param("id") Integer id);

    @Query("""
        SELECT o
        FROM Order o
        JOIN o.details od
        WHERE o.user.id = :userId AND o.status = 'PENDING' AND od.book.id IN (:bookIds)
        GROUP BY o
        HAVING COUNT(od.id) = :size AND (SELECT COUNT(od2.id) FROM OrderDetail od2 WHERE od2.order = o) = :size
        """)
    Optional<Order> findWithDetailsByUserIdAndBookIdsIn(
            @Param("userId") Integer userId,
            @Param("bookIds") List<Integer> bookIds,
            @Param("size") long size);
}
