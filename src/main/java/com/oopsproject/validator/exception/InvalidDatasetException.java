package com.oopsproject.validator.exception;

/**
 * Thrown when the dataset file is missing, malformed, or unreadable.
 * Owner: [Teammate name here]
 */
public class InvalidDatasetException extends Exception {
    public InvalidDatasetException(String message) {
        super(message);
    }

    public InvalidDatasetException(String message, Throwable cause) {
        super(message, cause);
    }
}
