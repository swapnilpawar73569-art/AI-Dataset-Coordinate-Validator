package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.List;

/**
 * Abstract base class for all validation strategy implementations (Inheritance & Template Method Pattern).
 */
public abstract class AbstractValidator implements Validator {

    protected boolean isNullOrUnparseable(Coordinate coordinate) {
        return coordinate == null || !coordinate.isParseable();
    }

    @Override
    public boolean isValid(Coordinate coordinate, List<Coordinate> dataset) {
        if (coordinate == null) {
            return false;
        }
        return doValidate(coordinate, dataset);
    }

    /**
     * Primitive method to be implemented by concrete validator strategy subclasses.
     */
    protected abstract boolean doValidate(Coordinate coordinate, List<Coordinate> dataset);
}
