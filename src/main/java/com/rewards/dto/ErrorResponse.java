package com.rewards.dto;

import java.time.LocalDateTime;

/**
 * Standard error response returned by the API for all error scenarios.
 *
 * @param status    HTTP status code
 * @param error     short error description (e.g. "Not Found")
 * @param message   detailed error message
 * @param timestamp time the error occurred
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp) {
}
