package com.harry.clio.service;

import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserOption;
import com.harry.clio.dto.user.UserResponse;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(CreateUserRequest request);

    UserResponse getUserById(int id);

    List<UserOption> getUserOptions();
}
