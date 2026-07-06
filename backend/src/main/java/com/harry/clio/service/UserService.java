package com.harry.clio.service;

import com.harry.clio.dto.CreateUserRequest;
import com.harry.clio.dto.UserResponse;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(CreateUserRequest request);

    UserResponse getCurrentUser(int id);
}
