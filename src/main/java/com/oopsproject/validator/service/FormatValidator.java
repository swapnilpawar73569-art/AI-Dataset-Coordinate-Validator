package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that latitude and longitude values are present and correctly formatted numbers.
 * Extends AbstractValidator (Inheritance).
 */
public class FormatValidator extends AbstractValidator {

    @Override
    public String getRuleName() {
        return "Format Check";
    }

    @Override
    protected boolean doValidate(Coordinate coordinate, List<Coordinate> dataset) {
        return !coordinate.hasMissingValue() && !coordinate.hasFormatError();
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (coordinate == null) return "Coordinate record is null";

        List<String> errors = new ArrayList<>();
        if (coordinate.isLatitudeMissing()) {
            errors.add("Latitude is missing");
        } else if (coordinate.isLatitudeInvalidFormat()) {
            errors.add("Latitude is not a valid number ('" + coordinate.getRawLatitude() + "')");
        }

        if (coordinate.isLongitudeMissing()) {
            errors.add("Longitude is missing");
        } else if (coordinate.isLongitudeInvalidFormat()) {
            errors.add("Longitude is not a valid number ('" + coordinate.getRawLongitude() + "')");
        }

        return String.join(", ", errors);
    }
}
