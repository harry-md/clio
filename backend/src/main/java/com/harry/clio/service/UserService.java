package com.harry.clio.service;

import com.harry.clio.dto.user.AdminUserListResponse;
import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserFilterRequest;
import com.harry.clio.dto.user.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(CreateUserRequest request);

    UserResponse getUserById(int id);

    Page<AdminUserListResponse> getAllAdminUsers(UserFilterRequest request, Pageable pageable);
}
