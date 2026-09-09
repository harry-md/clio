package com.harry.clio.repository;

import com.harry.clio.dto.stats.RevenuePointRow;
import com.harry.clio.dto.stats.TopBookRevenueRow;
import com.harry.clio.model.OrderStatus;
import com.harry.clio.model.RevenueLog;
import com.harry.clio.model.RevenueLogOwner;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RevenueLogRepository extends JpaRepository<RevenueLog, Integer> {
    @Query("""
        SELECT rl
        FROM RevenueLog rl
        JOIN FETCH rl.publisher
        WHERE rl.computed = :is_computed AND rl.createdAt < :cutoff AND rl.owner = :owner
        """)
    List<RevenueLog> findAllWithPublisherBefore(
            @Param("is_computed") boolean computed,
            @Param("cutoff") Instant cutoff,
            @Param("owner") RevenueLogOwner owner);

    @Query("""
        SELECT new com.harry.clio.dto.stats.RevenuePointRow(
            CAST(SQL('date_part(?, ? AT TIME ZONE ?)', :unit, rl.createdAt, :zoneId)
                AS Integer
            ),
            SUM(
                CASE WHEN od.book IS NOT NULL
                    THEN rl.amount ELSE 0 END
            ),
            SUM(
                CASE WHEN od.subscriptionPlan IS NOT NULL
                    THEN rl.amount ELSE 0 END
            )
        )
        FROM RevenueLog rl
        JOIN rl.orderDetail od
        JOIN od.order o
        WHERE rl.owner = :owner AND o.status = :status AND rl.createdAt >= :start AND rl.createdAt < :end
        GROUP BY 1
        """)
    List<RevenuePointRow> findPlatformRevenueDetails(
            @Param("owner") RevenueLogOwner owner,
            @Param("status") OrderStatus status,
            @Param("unit") String unit,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("zoneId") String zoneId);

    @Query("""
        SELECT new com.harry.clio.dto.stats.TopBookRevenueRow(b.id, b.title, COUNT(rl.id), SUM(rl.amount))
        FROM RevenueLog rl
        JOIN rl.orderDetail od
        JOIN od.order o
        JOIN od.book b
        WHERE rl.owner = :owner AND o.status = :status AND rl.createdAt >= :start AND rl.createdAt < :end
        GROUP BY b.id, b.title
        ORDER BY SUM(rl.amount) DESC, COUNT(rl.id) DESC, b.id DESC
        """)
    List<TopBookRevenueRow> findTopPlatformRevenueBooks(
            @Param("owner") RevenueLogOwner owner,
            @Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);
}
