package com.harry.clio.repository;

import com.harry.clio.model.SubscriptionBookBilling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SubscriptionBookBillingRepository
        extends JpaRepository<SubscriptionBookBilling, Integer> {
    boolean existsByUserIdAndBookId(Integer userId, Integer bookId);

    @Query("""
        SELECT COALESCE(SUM(b.pageCount), 0)
        FROM SubscriptionBookBilling b
        WHERE b.createdAt >= :start AND b.createdAt < :end
        """)
    Long findMonthlyTotalPageCount(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
        SELECT bb
        FROM SubscriptionBookBilling bb
        JOIN FETCH bb.book b
        WHERE bb.createdAt >= :start AND bb.createdAt < :end
        """)
    List<SubscriptionBookBilling> findAllWithBookInMonth(
            @Param("start") Instant start, @Param("end") Instant end);
}
