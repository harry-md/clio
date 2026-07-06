package com.harry.clio.service;

import com.harry.clio.dto.UserCreateRequest;
import com.harry.clio.dto.UserResponse;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(UserCreateRequest request);

    UserResponse getCurrentUser(int id);
}
