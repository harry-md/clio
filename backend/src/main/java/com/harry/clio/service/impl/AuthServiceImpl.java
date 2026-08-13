package com.harry.clio.service.impl;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.login.AuthResponse;
import com.harry.clio.dto.login.LoginRequest;
import com.harry.clio.dto.login.LoginResult;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.service.AuthService;
import com.harry.clio.util.JwtUtil;
import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResult login(LoginRequest request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));
            CustomUser principal = (CustomUser) authentication.getPrincipal();

            String token = jwtUtil.generateToken(principal);
            AuthResponse authResponse = new AuthResponse(
                    principal.getUsername(),
                    principal.getFirstName(),
                    principal.getLastName(),
                    principal.getAvatar(),
                    principal.getRole());
            return new LoginResult(token, authResponse);
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        } catch (JOSEException ex) {
            throw new RuntimeException("Có lỗi xảy ra");
        }
    }
}
