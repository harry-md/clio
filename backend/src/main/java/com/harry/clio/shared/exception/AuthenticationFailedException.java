package com.harry.clio.shared.exception;

import lombok.Getter;

@Getter
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
