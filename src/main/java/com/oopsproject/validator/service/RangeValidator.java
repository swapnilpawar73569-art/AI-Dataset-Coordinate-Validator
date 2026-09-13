package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that latitude is within [-90.0, 90.0] and longitude is within [-180.0, 180.0].
 * Extends AbstractValidator (Inheritance).
 */
public class RangeValidator extends AbstractValidator {

    public static final double MIN_LATITUDE = -90.0;
    public static final double MAX_LATITUDE = 90.0;
    public static final double MIN_LONGITUDE = -180.0;
    public static final double MAX_LONGITUDE = 180.0;

    @Override
    public String getRuleName() {
        return "Range Check";
    }

    @Override
    protected boolean doValidate(Coordinate coordinate, List<Coordinate> dataset) {
        if (!coordinate.isParseable()) {
            return true;
        }
        double lat = coordinate.getLatitude();
        double lon = coordinate.getLongitude();
        return lat >= MIN_LATITUDE && lat <= MAX_LATITUDE && lon >= MIN_LONGITUDE && lon <= MAX_LONGITUDE;
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (isNullOrUnparseable(coordinate) || isValid(coordinate, dataset)) {
            return "";
        }

        List<String> errors = new ArrayList<>();
        double lat = coordinate.getLatitude();
        double lon = coordinate.getLongitude();

        if (lat < MIN_LATITUDE || lat > MAX_LATITUDE) {
            errors.add(String.format("Latitude %.4f out of valid range [-90, 90]", lat));
        }

        if (lon < MIN_LONGITUDE || lon > MAX_LONGITUDE) {
            errors.add(String.format("Longitude %.4f out of valid range [-180, 180]", lon));
        }

        return String.join(", ", errors);
    }
}
