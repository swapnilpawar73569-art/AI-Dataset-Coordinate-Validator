package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that coordinates fall within a specific regional bounding box (Inheritance & Strategy Pattern).
 */
public class BoundingBoxValidator extends AbstractValidator {

    private final String regionName;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;

    /**
     * Default constructor initializes bounding box for India regional coordinates.
     */
    public BoundingBoxValidator() {
        this("India Region", 6.0, 37.5, 68.0, 97.5);
    }

    public BoundingBoxValidator(String regionName, double minLatitude, double maxLatitude,
                                double minLongitude, double maxLongitude) {
        this.regionName = regionName != null ? regionName : "Custom Region";
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    @Override
    public String getRuleName() {
        return "Bounding Box Check (" + regionName + ")";
    }

    @Override
    protected boolean doValidate(Coordinate coordinate, List<Coordinate> dataset) {
        if (!coordinate.isParseable()) {
            return true;
        }
        double lat = coordinate.getLatitude();
        double lon = coordinate.getLongitude();
        return lat >= minLatitude && lat <= maxLatitude && lon >= minLongitude && lon <= maxLongitude;
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (isNullOrUnparseable(coordinate) || isValid(coordinate, dataset)) {
            return "";
        }

        List<String> errors = new ArrayList<>();
        double lat = coordinate.getLatitude();
        double lon = coordinate.getLongitude();

        if (lat < minLatitude || lat > maxLatitude) {
            errors.add(String.format("Latitude %.4f outside %s bounds [%.1f, %.1f]",
                    lat, regionName, minLatitude, maxLatitude));
        }

        if (lon < minLongitude || lon > maxLongitude) {
            errors.add(String.format("Longitude %.4f outside %s bounds [%.1f, %.1f]",
                    lon, regionName, minLongitude, maxLongitude));
        }

        return String.join(", ", errors);
    }
}
