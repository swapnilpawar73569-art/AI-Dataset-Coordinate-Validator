package com.oopsproject.validator.exception;

/**
 * Thrown when an error occurs during the validation execution pipeline.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
