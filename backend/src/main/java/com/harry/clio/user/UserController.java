package com.harry.clio.user;

import com.harry.clio.security.CustomUser;

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
class UserController {
    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<UserResponse> register(@Valid @ModelAttribute UserCreateRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUser user) {
        return new ResponseEntity<>(userService.getCurrentUser(user.getId()), HttpStatus.OK);
    }
}
