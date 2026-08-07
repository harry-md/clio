package com.harry.clio.repository;

import com.harry.clio.entity.Subscription;
import com.harry.clio.entity.SubscriptionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    boolean existsByUserIdAndStatus(Integer userId, SubscriptionStatus status);

    Optional<Subscription> findByUserIdAndStatus(Integer userId, SubscriptionStatus status);

    @Transactional
    @Modifying
    @Query("""
        UPDATE Subscription s
        SET s.status = :expired
        WHERE s.status = :active AND s.endDate <= :date
        """)
    int expireSubscriptions(
            @Param("date") LocalDate date,
            @Param("expired") SubscriptionStatus expired,
            @Param("active") SubscriptionStatus active);
}
