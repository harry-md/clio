package com.harry.clio.repository;

import com.harry.clio.entity.SubscriptionAllocation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionAllocationRepository
        extends JpaRepository<SubscriptionAllocation, Integer> {}
