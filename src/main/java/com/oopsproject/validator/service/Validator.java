package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.List;

/**
 * Contract for validation rule strategies (Strategy Pattern).
 */
public interface Validator {
    /**
     * Checks if coordinate complies with this validation strategy rule.
     *
     * @param coordinate the coordinate to evaluate
     * @param dataset full dataset context for whole-dataset rules (duplicates, outliers)
     * @return true if valid, false if violation detected
     */
    boolean isValid(Coordinate coordinate, List<Coordinate> dataset);

    /**
     * Returns a human-readable failure reason if validation fails.
     *
     * @param coordinate the coordinate to evaluate
     * @param dataset full dataset context
     * @return error message string, or empty string if valid
     */
    String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset);

    /**
     * Returns rule identifier name.
     */
    String getRuleName();
}
