package com.harry.clio.entity;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "publishers")
public class Publisher {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String bankAccountNumber;

    @Builder.Default
    @Column(nullable = false, scale = 2, precision = 15)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher")
    private Set<Book> books;
}
