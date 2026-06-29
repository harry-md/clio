package com.harry.clio.user;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(UserCreateRequest request);

    UserResponse getCurrentUser(int id);
}
