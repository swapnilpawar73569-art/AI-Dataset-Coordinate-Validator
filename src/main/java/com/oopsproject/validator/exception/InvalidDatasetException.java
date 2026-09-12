package com.oopsproject.validator.exception;

/**
 * Base checked exception thrown when dataset file operations fail or datasets are invalid.
 */
public class InvalidDatasetException extends Exception {
    public InvalidDatasetException(String message) {
        super(message);
    }

    public InvalidDatasetException(String message, Throwable cause) {
        super(message, cause);
    }
}
