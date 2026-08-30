package com.harry.clio.repository;

import com.harry.clio.model.RevenueLog;
import com.harry.clio.model.RevenueLogOwner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RevenueLogRepository extends JpaRepository<RevenueLog, Integer> {
    @Query("""
        SELECT rl
        FROM RevenueLog rl
        JOIN FETCH rl.publisher
        WHERE rl.computed = :is_computed AND rl.createdAt < :cutoff AND rl.owner = :owner
        """)
    List<RevenueLog> findAllWithPublisherBefore(
            @Param("is_computed") boolean computed,
            @Param("cutoff") Instant cutoff,
            @Param("owner") RevenueLogOwner owner);
}
