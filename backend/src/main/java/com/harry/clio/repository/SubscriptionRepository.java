package com.harry.clio.repository;

import com.harry.clio.entity.Subscription;
import com.harry.clio.entity.SubscriptionStatus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    boolean existsByUserIdAndStatus(Integer userId, SubscriptionStatus status);
}
