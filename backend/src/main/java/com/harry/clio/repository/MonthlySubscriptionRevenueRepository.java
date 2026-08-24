package com.harry.clio.repository;

import com.harry.clio.model.MonthlySubscriptionRevenue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlySubscriptionRevenueRepository
        extends JpaRepository<MonthlySubscriptionRevenue, Integer> {
    Optional<MonthlySubscriptionRevenue> findByYearAndMonth(int year, int month);

    boolean existsByYearAndMonth(int year, int month);
}
