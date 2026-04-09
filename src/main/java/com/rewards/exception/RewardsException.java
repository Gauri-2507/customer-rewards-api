package com.rewards.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for domain errors. Carries the HTTP status to return.
 */
public class RewardsException extends RuntimeException {

    private final HttpStatus status;

    public RewardsException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
