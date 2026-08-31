package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
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
    private int month;

    @Column(nullable = false)
    private int year;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal publisherAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate startAllocateDate;

    @Column(nullable = false)
    private LocalDate endAllocateDate;
}
