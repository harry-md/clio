package com.harry.clio.repository;

import com.harry.clio.entity.OrderDetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("""
        SELECT od
        FROM OrderDetail od
        LEFT JOIN FETCH od.book
        LEFT JOIN FETCH od.subscriptionPlan
        WHERE od.order.id = :orderId
        """)
    List<OrderDetail> findAllWithItemByOrderId(@Param("orderId") Integer orderId);
}
