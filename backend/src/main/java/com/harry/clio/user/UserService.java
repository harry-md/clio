package com.harry.clio.domain.user;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserResponse register(CreateUserRequest request);
}
