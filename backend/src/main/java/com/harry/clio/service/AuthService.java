package com.harry.clio.service;

import com.harry.clio.dto.LoginRequest;
import com.harry.clio.dto.LoginResult;

public interface AuthService {
    LoginResult login(LoginRequest request);
}
