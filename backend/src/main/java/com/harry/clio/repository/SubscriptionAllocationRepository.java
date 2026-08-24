package com.harry.clio.repository;

import com.harry.clio.model.SubscriptionAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface SubscriptionAllocationRepository
        extends JpaRepository<SubscriptionAllocation, Integer> {
    @Query("""
        SELECT COALESCE(SUM(sa.publisherAmount), 0)
        FROM SubscriptionAllocation sa
        WHERE sa.month = :month AND sa.year = :year
        """)
    BigDecimal findTotalPublisherAmountByMonthAndYearAndStatus(
            @Param("year") int year, @Param("month") int month);
}
