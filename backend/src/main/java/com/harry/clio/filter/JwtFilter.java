package com.harry.clio.filter;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.util.JwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private static final String JWT_COOKIE_NAME = "jwt_token";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            try {
                JWTClaimsSet claimsSet = jwtUtil.validateTokenAndGetClaims(token);
                if (claimsSet != null) {
                    Integer userId = claimsSet.getIntegerClaim("userId");
                    String username = claimsSet.getSubject();
                    String firstName = claimsSet.getStringClaim("firstName");
                    String lastName = claimsSet.getStringClaim("lastName");
                    String avatar = claimsSet.getStringClaim("avatar");
                    request.setAttribute("username", username);
                    String role = claimsSet.getStringClaim("role");
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    CustomUser principal = new CustomUser(
                            userId, username, "", firstName, lastName, avatar, role, authorities);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
