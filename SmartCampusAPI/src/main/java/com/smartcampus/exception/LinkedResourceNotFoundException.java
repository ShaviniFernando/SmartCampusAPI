package com.smartcampus.exception;

/**
 * Thrown when a POST request references a roomId that does not exist.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
