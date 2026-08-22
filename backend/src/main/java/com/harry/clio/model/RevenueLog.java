package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "revenue_logs",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_revenue_logs_order_detail_id_owner",
                    columnNames = {"order_detail_id", "owner"})
        })
public class RevenueLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revenue_log_seq")
    @SequenceGenerator(
            name = "revenue_log_seq",
            sequenceName = "revenue_log_seq",
            allocationSize = 50)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RevenueLogOwner owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "publisher_id", nullable = true)
    private Publisher publisher;

    @Builder.Default
    @Column(name = "is_computed", nullable = false)
    private boolean computed = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
