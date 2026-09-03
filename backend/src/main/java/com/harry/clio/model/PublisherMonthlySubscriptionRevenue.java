package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "publisher_monthly_subscription_revenues",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_publisher_monthly_subscription_revenues_monthly_revenue_publisher",
                    columnNames = {"monthly_subscription_revenue_id", "publisher_id"})
        })
public class PublisherMonthlySubscriptionRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monthly_subscription_revenue_id", nullable = false)
    private MonthlySubscriptionRevenue monthlySubscriptionRevenue;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private long pageCount = 0L;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
