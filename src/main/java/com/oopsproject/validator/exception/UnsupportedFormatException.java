package com.oopsproject.validator.exception;

/**
 * Thrown when an unsupported file format/extension is supplied to the loader.
 */
public class UnsupportedFormatException extends InvalidDatasetException {
    public UnsupportedFormatException(String message) {
        super(message);
    }
}
