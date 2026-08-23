package com.harry.clio.repository;

import com.harry.clio.model.DetailType;
import com.harry.clio.model.Order;
import com.harry.clio.model.OrderStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") int id);

    @Query("""
        SELECT o
        FROM Order o
        JOIN o.details od
        WHERE o.user.id = :userId AND o.status = :status AND od.book.id IN (:bookIds)
        GROUP BY o
        HAVING COUNT(od.id) = :size AND (SELECT COUNT(od2.id) FROM OrderDetail od2 WHERE od2.order = o) = :size
        """)
    Optional<Order> findWithDetailsByUserIdAndBookIdsIn(
            @Param("userId") Integer userId,
            @Param("bookIds") List<Integer> bookIds,
            @Param("status") OrderStatus status,
            @Param("size") long size);

    @Query("""
        SELECT o
        FROM Order o
        JOIN FETCH o.details od
        WHERE o.user.id = :userId AND o.status = :status AND od.type = :type
        """)
    Optional<Order> findSubOrderWithDetailByUserId(
            @Param("userId") Integer userId,
            @Param("status") OrderStatus status,
            @Param("type") DetailType type);
}
