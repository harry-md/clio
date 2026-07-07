package com.harry.clio.service;

import com.harry.clio.dto.login.LoginRequest;
import com.harry.clio.dto.login.LoginResult;

public interface AuthService {
    LoginResult login(LoginRequest request);
}
