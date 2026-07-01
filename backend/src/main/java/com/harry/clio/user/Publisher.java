package com.harry.clio.user;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "publishers",
        indexes = {@Index(name = "idx_publishers_user", columnList = "user_id")})
public class Publisher {
    @Id
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String bankAccountNumber;

    @Column(nullable = false, scale = 15, precision = 2)
    private BigDecimal balance = BigDecimal.ZERO;
}
