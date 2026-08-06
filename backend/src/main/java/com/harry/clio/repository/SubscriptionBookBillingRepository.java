package com.harry.clio.repository;

import com.harry.clio.entity.SubscriptionBookBilling;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionBookBillingRepository
        extends JpaRepository<SubscriptionBookBilling, Integer> {
    boolean existsByUserIdAndBookId(Integer userId, Integer bookId);
}
