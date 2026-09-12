package com.oopsproject.validator.exception;

/**
 * Thrown when a dataset file cannot be read, found, or parsed due to IO/format issues.
 */
public class DatasetReadException extends InvalidDatasetException {
    public DatasetReadException(String message) {
        super(message);
    }

    public DatasetReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
