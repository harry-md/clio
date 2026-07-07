package com.harry.clio.service;

import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserResponse;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(CreateUserRequest request);

    UserResponse getCurrentUser(int id);

    UserResponse getUserById(int id);
}
