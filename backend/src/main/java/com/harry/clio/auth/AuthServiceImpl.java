package com.harry.clio.auth;

import com.harry.clio.security.CustomUser;
import com.harry.clio.security.JwtUtil;
import com.harry.clio.shared.exception.AuthenticationFailedException;
import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        CustomUser principal = (CustomUser) authentication.getPrincipal();

        try {
            String token = jwtUtil.generateToken(principal);
            AuthResponse authResponse = new AuthResponse(
                    principal.getUsername(),
                    principal.getFirstName(),
                    principal.getLastName(),
                    principal.getAvatar(),
                    principal.getRole());
            return new LoginResult(token, authResponse);

        } catch (JOSEException ex) {
            log.error("Lỗi tạo JWT token: {}", ex);
            throw new AuthenticationFailedException("Lỗi đăng nhập");
        }
    }
}
