package com.harry.clio.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
