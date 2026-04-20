package com.smartcampus.exception;

import java.time.Instant;

public class ErrorResponse {
    private String error;
    private int status;
    private long timestamp;

    public ErrorResponse() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public ErrorResponse(String error, int status) {
        this();
        this.error = error;
        this.status = status;
    }

    // Getters and Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
