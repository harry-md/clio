package com.harry.clio.repository;

import com.harry.clio.entity.Publisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    @Query("""
        SELECT p
        FROM Publisher p
        JOIN FETCH p.user
        """)
    List<Publisher> findAllWithDetail();

    @Query("""
        SELECT p
        FROM Publisher p
        JOIN FETCH p.user u
        WHERE u.id = :userId
        """)
    Optional<Publisher> findWithDetailByUserId(@Param("userId") int userId);
}
