package com.harry.clio.repository;

import com.harry.clio.dto.stats.TopSellingBookResponse;
import com.harry.clio.model.DetailType;
import com.harry.clio.model.OrderDetail;
import com.harry.clio.model.OrderStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface StatRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("""
        SELECT new com.harry.clio.dto.stats.TopSellingBookResponse(b.id, b.title, b.thumbnail, COUNT(b.id))
        FROM OrderDetail od
        JOIN od.order o
        JOIN od.book b
        WHERE b.publisher.userId = :publisherId
            AND o.status = :orderStatus
            AND od.type = :detailType
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
        GROUP BY b.id
        ORDER BY COUNT(od.id) DESC, b.id DESC
        """)
    List<TopSellingBookResponse> findTopSellingBooksByPublisherId(
            @Param("publisherId") int publisherId,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("detailType") DetailType detailType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}
