package com.oopsproject.validator.model;

/**
 * Represents a single geographic/data coordinate record from the dataset.
 * Owner: [Teammate name here]
 */
public class Coordinate {
    private double latitude;
    private double longitude;
    private String label; // optional dataset row identifier

    public Coordinate(double latitude, double longitude, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "label='" + label + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
