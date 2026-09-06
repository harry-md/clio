package com.harry.clio.repository;

import com.harry.clio.model.RevenueLog;
import com.harry.clio.model.RevenueLogOwner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface RevenueLogRepository extends JpaRepository<RevenueLog, Integer> {
    @Query("""
        SELECT rl
        FROM RevenueLog rl
        JOIN FETCH rl.publisher
        WHERE rl.computed = :is_computed
            AND rl.createdAt < :cutoff
            AND rl.owner = :owner
        """)
    List<RevenueLog> findAllWithPublisherBefore(
            @Param("is_computed") boolean computed,
            @Param("cutoff") Instant cutoff,
            @Param("owner") RevenueLogOwner owner);

    interface RevenuePointRow {
        Integer getBucket();

        BigDecimal getBookRevenue();

        BigDecimal getSubscriptionRevenue();
    }

    interface TopBookRevenueRow {
        Integer getBookId();

        String getTitle();

        Long getSales();

        BigDecimal getRevenue();
    }

    @Query(value = """
        SELECT
            CAST(
                DATE_PART(
                    :unit,
                    rl.created_at AT TIME ZONE :zoneId
                ) AS INTEGER
            ) AS "bucket",
            SUM(
                CASE WHEN od.book_id IS NOT NULL
                    THEN rl.amount ELSE 0 END
            ) AS "bookRevenue",
            SUM(
                CASE WHEN od.subscription_plan_id IS NOT NULL
                    THEN rl.amount ELSE 0 END
            ) AS "subscriptionRevenue"
        FROM revenue_logs rl
        JOIN order_details od ON od.id = rl.order_detail_id
        JOIN orders o ON o.id = od.order_id
        WHERE rl.owner = :owner
            AND o.status = :status
            AND rl.created_at >= :start
            AND rl.created_at < :end
        GROUP BY 1
        ORDER BY 1
        """, nativeQuery = true)
    List<RevenuePointRow> findPlatformRevenueDetails(
            @Param("owner") String owner,
            @Param("status") String status,
            @Param("unit") String unit,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("zoneId") String zoneId);

    @Query(value = """
        SELECT
            b.id AS "bookId",
            b.title AS "title",
            COUNT(rl.id) AS "sales",
            SUM(rl.amount) AS "revenue"
        FROM revenue_logs rl
        JOIN order_details od ON od.id = rl.order_detail_id
        JOIN orders o ON o.id = od.order_id
        JOIN books b ON b.id = od.book_id
        WHERE rl.owner = :owner
            AND o.status = :status
            AND rl.created_at >= :start
            AND rl.created_at < :end
        GROUP BY b.id, b.title
        ORDER BY SUM(rl.amount) DESC, COUNT(rl.id) DESC, b.id DESC
        LIMIT 5
        """, nativeQuery = true)
    List<TopBookRevenueRow> findTopPlatformRevenueBooks(
            @Param("owner") String owner,
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
