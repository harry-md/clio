package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
            @UniqueConstraint(name = "uq_users_email", columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @ColumnDefault(
            "'https://res.cloudinary.com/dswxedhsf/image/upload/v1782626276/avatar_qoprdc.png'")
    private String avatar =
            "https://res.cloudinary.com/dswxedhsf/image/upload/v1782626276/avatar_qoprdc.png";

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.READER;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
