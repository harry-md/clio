package com.harry.clio.repository;

import com.harry.clio.entity.Publisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    @Query("""
        SELECT p
        FROM Publisher p
        JOIN FETCH p.user
        """)
    List<Publisher> findAllWithUser();

    @Query("""
        SELECT p
        FROM Publisher p
        JOIN FETCH p.user u
        WHERE u.id = :userId
        """)
    Optional<Publisher> findWithUserByUserId(@Param("userId") int userId);

    @Modifying
    @Query("""
        UPDATE Publisher p
        SET p.balance = p.balance + :amount
        WHERE p.userId = :publisherId
        """)
    int increaseBalance(
            @Param("publisherId") Integer publisherId, @Param("amount") BigDecimal amount);
}
