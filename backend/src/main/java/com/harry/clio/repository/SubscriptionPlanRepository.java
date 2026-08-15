package com.harry.clio.repository;

import com.harry.clio.model.SubscriptionPlan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    List<SubscriptionPlan> findAllByActiveTrue();

    Optional<SubscriptionPlan> findByIdAndActiveTrue(Integer id);
}
