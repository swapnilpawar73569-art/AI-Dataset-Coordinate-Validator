package com.oopsproject.validator.service;

import com.oopsproject.validator.exception.DatasetReadException;
import com.oopsproject.validator.model.Coordinate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DatasetLoader implementation for CSV files using Apache Commons CSV.
 */
public class CsvDatasetLoader implements DatasetLoader {

    @Override
    public List<Coordinate> load(String filePath) throws DatasetReadException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new DatasetReadException("File path cannot be null or empty.");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new DatasetReadException("Dataset file not found: " + filePath);
        }
        if (!file.isFile()) {
            throw new DatasetReadException("Path is not a regular file: " + filePath);
        }

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            return load(reader);
        } catch (IOException e) {
            throw new DatasetReadException("Failed to read CSV dataset file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Coordinate> load(Reader reader) throws DatasetReadException {
        if (reader == null) {
            throw new DatasetReadException("Reader cannot be null.");
        }

        List<Coordinate> coordinates = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .build();

        try (CSVParser parser = new CSVParser(reader, csvFormat)) {

            int rowCounter = 1;
            for (CSVRecord record : parser) {
                String label = getColumnValue(record, "label", "id", "name");
                String latStr = getColumnValue(record, "latitude", "lat");
                String lonStr = getColumnValue(record, "longitude", "lon", "lng", "long");

                boolean latMissing = latStr == null || latStr.trim().isEmpty();
                boolean lonMissing = lonStr == null || lonStr.trim().isEmpty();

                Double latitude = null;
                boolean latInvalidFormat = false;
                if (!latMissing) {
                    try {
                        latitude = Double.parseDouble(latStr.trim());
                    } catch (NumberFormatException e) {
                        latInvalidFormat = true;
                    }
                }

                Double longitude = null;
                boolean lonInvalidFormat = false;
                if (!lonMissing) {
                    try {
                        longitude = Double.parseDouble(lonStr.trim());
                    } catch (NumberFormatException e) {
                        lonInvalidFormat = true;
                    }
                }

                Coordinate coord = new Coordinate(
                        rowCounter++,
                        label != null ? label.trim() : "Row_" + rowCounter,
                        latStr != null ? latStr.trim() : "",
                        lonStr != null ? lonStr.trim() : "",
                        latitude,
                        longitude,
                        latMissing,
                        lonMissing,
                        latInvalidFormat,
                        lonInvalidFormat
                );

                coordinates.add(coord);
            }
        } catch (IOException e) {
            throw new DatasetReadException("Failed to read CSV dataset file: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new DatasetReadException("Malformed CSV structure or header: " + e.getMessage(), e);
        }

        return coordinates;
    }

    private String getColumnValue(CSVRecord record, String... possibleNames) {
        for (String name : possibleNames) {
            if (record.isMapped(name)) {
                return record.get(name);
            }
        }
        // Fallback to column index if headers weren't named expectedly
        if (possibleNames[0].equalsIgnoreCase("label") && record.size() >= 1) {
            return record.get(0);
        } else if (possibleNames[0].equalsIgnoreCase("latitude") && record.size() >= 2) {
            return record.get(1);
        } else if (possibleNames[0].equalsIgnoreCase("longitude") && record.size() >= 3) {
            return record.get(2);
        }
        return null;
    }
}
