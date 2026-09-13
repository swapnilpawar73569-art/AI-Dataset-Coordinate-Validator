package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that coordinate records do not repeat identical latitude and longitude values across the dataset.
 * Extends AbstractValidator (Inheritance).
 */
public class DuplicateValidator extends AbstractValidator {

    @Override
    public String getRuleName() {
        return "Duplicate Check";
    }

    @Override
    protected boolean doValidate(Coordinate coordinate, List<Coordinate> dataset) {
        if (!coordinate.isParseable() || dataset == null) {
            return true;
        }

        for (Coordinate other : dataset) {
            if (other.getRowNumber() != coordinate.getRowNumber() && other.isParseable()) {
                if (coordinate.equals(other)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (isNullOrUnparseable(coordinate) || dataset == null || isValid(coordinate, dataset)) {
            return "";
        }

        List<Integer> matchingRows = new ArrayList<>();
        for (Coordinate other : dataset) {
            if (other.getRowNumber() != coordinate.getRowNumber() && other.isParseable() && coordinate.equals(other)) {
                matchingRows.add(other.getRowNumber());
            }
        }

        if (!matchingRows.isEmpty()) {
            return "Duplicate coordinate matches Row(s): " + matchingRows;
        }
        return "";
    }
}
