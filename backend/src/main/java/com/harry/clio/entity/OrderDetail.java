package com.harry.clio.entity;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "book_id", nullable = true)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "subscription_plan_id", nullable = true)
    private SubscriptionPlan subscriptionPlan;

    @Builder.Default
    @Column(name = "book_title", nullable = true)
    private String bookTitle = "";

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private OrderDetailType type = OrderDetailType.BOOK;
}
