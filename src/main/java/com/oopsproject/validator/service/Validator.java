package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

/**
 * Contract for all validation rule classes.
 * Implement this for each rule (RangeValidator, FormatValidator, DuplicateValidator, etc.)
 * so new rules can be plugged in without changing existing code (Open/Closed Principle).
 *
 * Owner: [Teammate name here]
 */
public interface Validator {
    /**
     * @param coordinate the coordinate to check
     * @return true if valid, false if it violates this rule
     */
    boolean isValid(Coordinate coordinate);

    /**
     * @return a human-readable description of why validation failed,
     *         to be shown in the report.
     */
    String getErrorMessage(Coordinate coordinate);
}
