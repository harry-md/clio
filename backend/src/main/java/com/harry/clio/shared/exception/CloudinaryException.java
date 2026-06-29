package com.harry.clio.shared.exception;

import lombok.Getter;

@Getter
public class CloudinaryException extends RuntimeException {
    public CloudinaryException(String message) {
        super(message);
    }
}
