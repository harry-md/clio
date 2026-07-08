package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserResponse;
import com.harry.clio.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<UserResponse> register(@Valid @ModelAttribute CreateUserRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUser user) {
        return new ResponseEntity<>(userService.getCurrentUser(user.getId()), HttpStatus.OK);
    }
}
