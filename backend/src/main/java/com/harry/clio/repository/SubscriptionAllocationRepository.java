package com.harry.clio.repository;

import com.harry.clio.model.SubscriptionAllocation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionAllocationRepository
        extends JpaRepository<SubscriptionAllocation, Integer> {}
