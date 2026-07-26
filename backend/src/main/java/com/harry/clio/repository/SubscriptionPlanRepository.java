package com.harry.clio.repository;

import com.harry.clio.entity.SubscriptionPlan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    Optional<SubscriptionPlan> findByIdAndActiveTrue(Integer id);

    List<SubscriptionPlan> findAllByActiveTrue();
}
