package com.harry.clio.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "subscription_allocations",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_sub_allocations_subscription_year_month",
                    columnNames = {"subscription_id", "year", "month"})
        })
public class SubscriptionAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private AllocationStatus status = AllocationStatus.PENDING;

    @Column(nullable = false)
    private LocalDate startAllocateDate;

    @Column(nullable = false)
    private LocalDate endAllocateDate;
}
