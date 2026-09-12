package com.oopsproject.validator.model;

import java.util.Objects;

/**
 * Represents a single geographic coordinate record from a dataset.
 * Supports missing or unparseable values gracefully for validation reporting.
 */
public class Coordinate {
    private int rowNumber;
    private String label;
    private Double latitude;
    private Double longitude;
    private String rawLatitude;
    private String rawLongitude;
    private boolean latitudeMissing;
    private boolean longitudeMissing;
    private boolean latitudeInvalidFormat;
    private boolean longitudeInvalidFormat;

    public Coordinate(int rowNumber, String label, Double latitude, Double longitude) {
        this(rowNumber, label, 
             latitude != null ? String.valueOf(latitude) : "", 
             longitude != null ? String.valueOf(longitude) : "", 
             latitude, longitude, 
             latitude == null, longitude == null, 
             false, false);
    }

    public Coordinate(int rowNumber, String label, String rawLatitude, String rawLongitude,
                      Double latitude, Double longitude,
                      boolean latitudeMissing, boolean longitudeMissing,
                      boolean latitudeInvalidFormat, boolean longitudeInvalidFormat) {
        this.rowNumber = rowNumber;
        this.label = label != null ? label : "";
        this.rawLatitude = rawLatitude != null ? rawLatitude : "";
        this.rawLongitude = rawLongitude != null ? rawLongitude : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitudeMissing = latitudeMissing;
        this.longitudeMissing = longitudeMissing;
        this.latitudeInvalidFormat = latitudeInvalidFormat;
        this.longitudeInvalidFormat = longitudeInvalidFormat;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getLabel() {
        return label;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getRawLatitude() {
        return rawLatitude;
    }

    public String getRawLongitude() {
        return rawLongitude;
    }

    public boolean isLatitudeMissing() {
        return latitudeMissing;
    }

    public boolean isLongitudeMissing() {
        return longitudeMissing;
    }

    public boolean isLatitudeInvalidFormat() {
        return latitudeInvalidFormat;
    }

    public boolean isLongitudeInvalidFormat() {
        return longitudeInvalidFormat;
    }

    public boolean hasMissingValue() {
        return latitudeMissing || longitudeMissing;
    }

    public boolean hasFormatError() {
        return latitudeInvalidFormat || longitudeInvalidFormat;
    }

    public boolean isParseable() {
        return !hasMissingValue() && !hasFormatError() && latitude != null && longitude != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Row " + rowNumber + " [" + label + "]: lat=" + 
                (latitudeMissing ? "<MISSING>" : (latitudeInvalidFormat ? "<INVALID_FORMAT: " + rawLatitude + ">" : latitude)) +
                ", lon=" + 
                (longitudeMissing ? "<MISSING>" : (longitudeInvalidFormat ? "<INVALID_FORMAT: " + rawLongitude + ">" : longitude));
    }
}
