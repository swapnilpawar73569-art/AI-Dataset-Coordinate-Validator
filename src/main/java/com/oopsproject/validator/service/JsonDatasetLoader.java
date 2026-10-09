package com.oopsproject.validator.service;

import com.oopsproject.validator.exception.DatasetReadException;
import com.oopsproject.validator.model.Coordinate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads coordinate datasets from JSON files using pure standard Java.
 * No external JSON parsing libraries required.
 */
public class JsonDatasetLoader implements DatasetLoader {

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
            throw new DatasetReadException("Failed to read JSON dataset file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Coordinate> load(Reader reader) throws DatasetReadException {
        if (reader == null) {
            throw new DatasetReadException("Reader cannot be null.");
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new DatasetReadException("Failed to read JSON stream: " + e.getMessage(), e);
        }

        return parseJsonString(content.toString());
    }

    @Override
    public List<Coordinate> loadFromString(String content) throws DatasetReadException {
        if (content == null) {
            throw new DatasetReadException("Dataset content cannot be null.");
        }
        return parseJsonString(content);
    }

    private List<Coordinate> parseJsonString(String json) {
        List<Coordinate> coordinates = new ArrayList<>();
        Pattern objectPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher objectMatcher = objectPattern.matcher(json);

        int rowCounter = 1;
        while (objectMatcher.find()) {
            String objectBody = objectMatcher.group(1);

            String label = extractField(objectBody, "label", "id", "name");
            String latRaw = extractField(objectBody, "latitude", "lat");
            String lonRaw = extractField(objectBody, "longitude", "lon", "lng", "long");

            boolean latMissing = latRaw == null || latRaw.trim().isEmpty() || latRaw.equalsIgnoreCase("null");
            boolean lonMissing = lonRaw == null || lonRaw.trim().isEmpty() || lonRaw.equalsIgnoreCase("null");

            Double latitude = null;
            boolean latInvalidFormat = false;
            if (!latMissing) {
                try {
                    latitude = Double.parseDouble(latRaw.trim());
                } catch (NumberFormatException e) {
                    latInvalidFormat = true;
                }
            }

            Double longitude = null;
            boolean lonInvalidFormat = false;
            if (!lonMissing) {
                try {
                    longitude = Double.parseDouble(lonRaw.trim());
                } catch (NumberFormatException e) {
                    lonInvalidFormat = true;
                }
            }

            Coordinate coord = new Coordinate(
                    rowCounter++,
                    label != null ? label : "Row_" + rowCounter,
                    latRaw != null && !latRaw.equalsIgnoreCase("null") ? latRaw : "",
                    lonRaw != null && !lonRaw.equalsIgnoreCase("null") ? lonRaw : "",
                    latitude,
                    longitude,
                    latMissing,
                    lonMissing,
                    latInvalidFormat,
                    lonInvalidFormat
            );

            coordinates.add(coord);
        }

        return coordinates;
    }

    private String extractField(String jsonBlock, String... fieldNames) {
        for (String field : fieldNames) {
            Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(\"[^\"]*\"|[^,\\s}]+)");
            Matcher m = p.matcher(jsonBlock);
            if (m.find()) {
                String val = m.group(1).trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1);
                }
                return val;
            }
        }
        return null;
    }
}
