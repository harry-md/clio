package com.harry.clio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(nullable = false, length = 255)
    @ColumnDefault(
            "'https://res.cloudinary.com/dswxedhsf/image/upload/v1782626276/avatar_qoprdc.png'")
    private String avatar =
            "https://res.cloudinary.com/dswxedhsf/image/upload/v1782626276/avatar_qoprdc.png";

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
}
