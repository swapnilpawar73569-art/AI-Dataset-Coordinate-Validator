package com.oopsproject.validator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the validation result for a single Coordinate row.
 */
public class ValidationResult {
    private final Coordinate coordinate;
    private final List<String> errorMessages;

    public ValidationResult(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.errorMessages = new ArrayList<>();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void addError(String errorMessage) {
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            this.errorMessages.add(errorMessage);
        }
    }

    public boolean isValid() {
        return errorMessages.isEmpty();
    }

    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    public String getFormattedErrors() {
        return String.join("; ", errorMessages);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "VALID | " + coordinate.toString();
        } else {
            return "INVALID | " + coordinate.toString() + " -> Reason(s): " + getFormattedErrors();
        }
    }
}
