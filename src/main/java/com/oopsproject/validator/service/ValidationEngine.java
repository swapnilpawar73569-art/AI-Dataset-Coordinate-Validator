package com.oopsproject.validator.service;

import com.oopsproject.validator.exception.ValidationException;
import com.oopsproject.validator.model.Coordinate;

import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates validation strategies and manages dataset validation pipeline execution.
 */
public class ValidationEngine {
    private final List<Validator> validators;

    public ValidationEngine() {
        this.validators = new ArrayList<>();
    }

    public ValidationEngine(List<Validator> defaultValidators) {
        this.validators = new ArrayList<>(defaultValidators != null ? defaultValidators : Collections.emptyList());
    }

    /**
     * Creates a ValidationEngine loaded with default Phase 1 & 2 validators.
     */
    public static ValidationEngine createDefaultEngine() {
        ValidationEngine engine = new ValidationEngine();
        engine.addValidator(new FormatValidator());
        engine.addValidator(new RangeValidator());
        engine.addValidator(new DuplicateValidator());
        engine.addValidator(new OutlierValidator());
        engine.addValidator(new PrecisionValidator());
        engine.addValidator(new BoundingBoxValidator());
        return engine;
    }

    public void addValidator(Validator validator) {
        if (validator != null) {
            this.validators.add(validator);
        }
    }

    public List<Validator> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    /**
     * Runs all registered validator strategies against each coordinate in the dataset.
     *
     * @param dataset list of coordinates to validate
     * @return ValidationReport containing detailed outcomes and statistics
     * @throws ValidationException if processing errors occur
     */
    public ValidationReport validate(List<Coordinate> dataset) throws ValidationException {
        if (dataset == null) {
            throw new ValidationException("Cannot validate null dataset.");
        }

        List<ValidationResult> results = new ArrayList<>();

        for (Coordinate coord : dataset) {
            ValidationResult result = new ValidationResult(coord);
            for (Validator validator : validators) {
                try {
                    if (!validator.isValid(coord, dataset)) {
                        String errorMsg = validator.getErrorMessage(coord, dataset);
                        result.addError(errorMsg);
                    }
                } catch (Exception e) {
                    result.addError("Error executing rule '" + validator.getRuleName() + "': " + e.getMessage());
                }
            }
            results.add(result);
        }

        return new ValidationReport(results);
    }
}
