package com.oopsproject.validator.service;

import com.oopsproject.validator.exception.DatasetReadException;
import com.oopsproject.validator.model.Coordinate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads coordinate datasets from CSV files using standard Java BufferedReader.
 * 
 * Simple to explain to Sir:
 * 1. Reads the file line-by-line using standard BufferedReader.
 * 2. Parses the header row to detect column positions for label, latitude, and longitude.
 * 3. Splits each data row by comma (",") using line.split().
 * 4. Parses numbers using Double.parseDouble() with try-catch for NumberFormatException.
 * 5. Returns a List of Coordinate objects.
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

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
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
        BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);

        try {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return coordinates; // Empty file
            }

            // Detect column indices from header row
            String[] headers = headerLine.split(",", -1);
            int labelIdx = 0;
            int latIdx = 1;
            int lonIdx = 2;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase();
                if (h.equals("label") || h.equals("name") || h.equals("id")) {
                    labelIdx = i;
                } else if (h.equals("latitude") || h.equals("lat")) {
                    latIdx = i;
                } else if (h.equals("longitude") || h.equals("lon") || h.equals("lng") || h.equals("long")) {
                    lonIdx = i;
                }
            }

            String line;
            int rowCounter = 1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty rows
                }

                String[] parts = line.split(",", -1);

                String label = (labelIdx < parts.length) ? parts[labelIdx].trim() : "Row_" + rowCounter;
                String latStr = (latIdx < parts.length) ? parts[latIdx].trim() : "";
                String lonStr = (lonIdx < parts.length) ? parts[lonIdx].trim() : "";

                boolean latMissing = latStr.isEmpty();
                boolean lonMissing = lonStr.isEmpty();

                Double latitude = null;
                boolean latInvalidFormat = false;
                if (!latMissing) {
                    try {
                        latitude = Double.parseDouble(latStr);
                    } catch (NumberFormatException e) {
                        latInvalidFormat = true;
                    }
                }

                Double longitude = null;
                boolean lonInvalidFormat = false;
                if (!lonMissing) {
                    try {
                        longitude = Double.parseDouble(lonStr);
                    } catch (NumberFormatException e) {
                        lonInvalidFormat = true;
                    }
                }

                Coordinate coord = new Coordinate(
                        rowCounter++,
                        label,
                        latStr,
                        lonStr,
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
        }

        return coordinates;
    }

    @Override
    public List<Coordinate> loadFromString(String content) throws DatasetReadException {
        if (content == null) {
            throw new DatasetReadException("Dataset content cannot be null.");
        }
        return load(new StringReader(content));
    }
}
