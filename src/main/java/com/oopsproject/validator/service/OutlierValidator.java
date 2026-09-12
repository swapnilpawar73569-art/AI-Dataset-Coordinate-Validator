package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects statistical outliers based on standard deviation distance from dataset centroid.
 */
public class OutlierValidator implements Validator {

    private final double sigmaThreshold;

    public OutlierValidator() {
        this(3.0); // Default 3-sigma rule
    }

    public OutlierValidator(double sigmaThreshold) {
        this.sigmaThreshold = sigmaThreshold;
    }

    @Override
    public String getRuleName() {
        return "Anomaly/Outlier Check";
    }

    @Override
    public boolean isValid(Coordinate coordinate, List<Coordinate> dataset) {
        if (coordinate == null || !coordinate.isParseable() || dataset == null) {
            return true;
        }

        List<Coordinate> validCoords = getParseableValidRangeCoords(dataset);
        if (validCoords.size() < 3) {
            // Need at least 3 points for statistical outlier calculation
            return true;
        }

        double meanLat = validCoords.stream().mapToDouble(Coordinate::getLatitude).average().orElse(0.0);
        double meanLon = validCoords.stream().mapToDouble(Coordinate::getLongitude).average().orElse(0.0);

        double stdLat = calculateStdDev(validCoords, meanLat, true);
        double stdLon = calculateStdDev(validCoords, meanLon, false);

        if (stdLat == 0.0 && stdLon == 0.0) {
            return true;
        }

        double zLat = stdLat > 0 ? Math.abs(coordinate.getLatitude() - meanLat) / stdLat : 0.0;
        double zLon = stdLon > 0 ? Math.abs(coordinate.getLongitude() - meanLon) / stdLon : 0.0;

        return zLat <= sigmaThreshold && zLon <= sigmaThreshold;
    }

    @Override
    public String getErrorMessage(Coordinate coordinate, List<Coordinate> dataset) {
        if (coordinate == null || !coordinate.isParseable() || dataset == null || isValid(coordinate, dataset)) {
            return "";
        }

        List<Coordinate> validCoords = getParseableValidRangeCoords(dataset);
        double meanLat = validCoords.stream().mapToDouble(Coordinate::getLatitude).average().orElse(0.0);
        double meanLon = validCoords.stream().mapToDouble(Coordinate::getLongitude).average().orElse(0.0);

        double stdLat = calculateStdDev(validCoords, meanLat, true);
        double stdLon = calculateStdDev(validCoords, meanLon, false);

        double zLat = stdLat > 0 ? Math.abs(coordinate.getLatitude() - meanLat) / stdLat : 0.0;
        double zLon = stdLon > 0 ? Math.abs(coordinate.getLongitude() - meanLon) / stdLon : 0.0;

        return String.format("Statistical outlier detected (Z-lat=%.2f, Z-lon=%.2f > threshold %.1f sigma)",
                zLat, zLon, sigmaThreshold);
    }

    private List<Coordinate> getParseableValidRangeCoords(List<Coordinate> dataset) {
        List<Coordinate> list = new ArrayList<>();
        RangeValidator rangeValidator = new RangeValidator();
        for (Coordinate c : dataset) {
            if (c.isParseable() && rangeValidator.isValid(c, dataset)) {
                list.add(c);
            }
        }
        return list;
    }

    private double calculateStdDev(List<Coordinate> coords, double mean, boolean isLatitude) {
        double variance = 0.0;
        for (Coordinate c : coords) {
            double val = isLatitude ? c.getLatitude() : c.getLongitude();
            variance += Math.pow(val - mean, 2);
        }
        return Math.sqrt(variance / coords.size());
    }
}
