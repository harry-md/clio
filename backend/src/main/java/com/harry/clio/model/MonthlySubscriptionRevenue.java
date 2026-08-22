package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "monthly_subscription_revenues",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_monthly_subscription_revenues_year_month",
                    columnNames = {"year", "month"})
        })
public class MonthlySubscriptionRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPublisherAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unallocatedAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPublisherAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Long totalPageCount = 0L;

    @Builder.Default
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private MonthlySubscriptionRevenueStatus status = MonthlySubscriptionRevenueStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
