package com.smartcampus.exception;

/**
 * Thrown when a requested resource (Room or Sensor) cannot be found by its ID.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
