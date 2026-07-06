package com.harry.clio.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "monthly_revenues",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_monthly_revenues_year_month",
                    columnNames = {"year", "month"})
        })
public class MonthlyRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal platformAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal publisherAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Long totalPageCount = 0L;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private MonthlyRevenueStatus status = MonthlyRevenueStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false, updatable = true)
    @UpdateTimestamp
    private Instant updatedAt;
}
