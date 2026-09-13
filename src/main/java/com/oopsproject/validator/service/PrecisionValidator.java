package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates coordinate decimal place precision and consistency.
 * Extends AbstractValidator (Inheritance).
 */
public class PrecisionValidator extends AbstractValidator {

    private final int maxDecimalPlaces;

    public PrecisionValidator() {
        this(8);
    }

    public PrecisionValidator(int maxDecimalPlaces) {
        this.maxDecimalPlaces = maxDecimalPlaces;
    }

    @Override
    public String getRuleName() {
        return "Precision Check";
    }

    @Override
    protected boolean doValidate(Coordinate coordinate, List<Coordinate> dataset) {
        if (!coordinate.isParseable()) {
            return true;
        }

        int latDecimals = getDecimalPlaces(coordinate.getRawLatitude());
        int lonDecimals = getDecimalPlaces(coordinate.getRawLongitude());

        return latDecimals <= maxDecimalPlaces && lonDecimals <= maxDecimalPlaces;
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (isNullOrUnparseable(coordinate) || isValid(coordinate, dataset)) {
            return "";
        }

        List<String> errors = new ArrayList<>();
        int latDecimals = getDecimalPlaces(coordinate.getRawLatitude());
        int lonDecimals = getDecimalPlaces(coordinate.getRawLongitude());

        if (latDecimals > maxDecimalPlaces) {
            errors.add(String.format("Latitude has excessive precision (%d decimal places > max %d)", latDecimals, maxDecimalPlaces));
        }

        if (lonDecimals > maxDecimalPlaces) {
            errors.add(String.format("Longitude has excessive precision (%d decimal places > max %d)", lonDecimals, maxDecimalPlaces));
        }

        return String.join(", ", errors);
    }

    private int getDecimalPlaces(String strVal) {
        if (strVal == null) return 0;
        String s = strVal.trim();
        int dotIdx = s.indexOf('.');
        if (dotIdx < 0 || dotIdx == s.length() - 1) {
            return 0;
        }
        return s.length() - dotIdx - 1;
    }
}
