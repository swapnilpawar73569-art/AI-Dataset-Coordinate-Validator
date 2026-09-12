package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that coordinate records do not repeat identical latitude and longitude values across the dataset.
 */
public class DuplicateValidator implements Validator {

    @Override
    public String getRuleName() {
        return "Duplicate Check";
    }

    @Override
    public boolean isValid(Coordinate coordinate, List<Coordinate> dataset) {
        if (coordinate == null || !coordinate.isParseable() || dataset == null) {
            return true;
        }

        for (Coordinate other : dataset) {
            if (other.getRowNumber() != coordinate.getRowNumber() && other.isParseable()) {
                if (coordinate.equals(other)) {
                    // Duplicate found
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (coordinate == null || !coordinate.isParseable() || dataset == null || isValid(coordinate, dataset)) {
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
