package com.smartcampus.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class InvalidInputException extends WebApplicationException {
    public InvalidInputException(String message) {
        super(Response.status(422) // Unprocessable Entity
                .entity(new ErrorResponse(message, 422))
                .build());
    }
}
