package com.harry.clio.filter;

import com.harry.clio.service.impl.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userKey = request.getRemoteAddr();
        if (!rateLimitService.allow(userKey)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().println("""
                {
                    "message": "Too many requests"
                }
                """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
