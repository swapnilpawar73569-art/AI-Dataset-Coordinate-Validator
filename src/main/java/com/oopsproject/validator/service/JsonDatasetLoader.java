package com.oopsproject.validator.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oopsproject.validator.exception.DatasetReadException;
import com.oopsproject.validator.model.Coordinate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DatasetLoader implementation for JSON files using Gson.
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

        List<Coordinate> coordinates = new ArrayList<>();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            JsonArray jsonArray = null;

            if (rootElement.isJsonArray()) {
                jsonArray = rootElement.getAsJsonArray();
            } else if (rootElement.isJsonObject()) {
                JsonObject rootObj = rootElement.getAsJsonObject();
                if (rootObj.has("coordinates") && rootObj.get("coordinates").isJsonArray()) {
                    jsonArray = rootObj.getAsJsonArray("coordinates");
                } else if (rootObj.has("data") && rootObj.get("data").isJsonArray()) {
                    jsonArray = rootObj.getAsJsonArray("data");
                } else {
                    throw new DatasetReadException("JSON object must contain a 'coordinates' or 'data' array.");
                }
            } else {
                throw new DatasetReadException("Invalid JSON root: expected array or object wrapper.");
            }

            int rowCounter = 1;
            for (JsonElement item : jsonArray) {
                if (!item.isJsonObject()) {
                    rowCounter++;
                    continue;
                }
                JsonObject obj = item.getAsJsonObject();

                String label = getStringProperty(obj, "label", "id", "name");
                JsonElement latElem = getProperty(obj, "latitude", "lat");
                JsonElement lonElem = getProperty(obj, "longitude", "lon", "lng", "long");

                String latStr = latElem != null && !latElem.isJsonNull() ? latElem.getAsString() : "";
                String lonStr = lonElem != null && !lonElem.isJsonNull() ? lonElem.getAsString() : "";

                boolean latMissing = latElem == null || latElem.isJsonNull() || latStr.trim().isEmpty();
                boolean lonMissing = lonElem == null || lonElem.isJsonNull() || lonStr.trim().isEmpty();

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
            throw new DatasetReadException("Failed to read JSON dataset file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DatasetReadException("Failed to parse JSON file structure: " + e.getMessage(), e);
        }

        return coordinates;
    }

    private JsonElement getProperty(JsonObject obj, String... names) {
        for (String name : names) {
            if (obj.has(name)) {
                return obj.get(name);
            }
        }
        return null;
    }

    private String getStringProperty(JsonObject obj, String... names) {
        JsonElement elem = getProperty(obj, names);
        if (elem != null && !elem.isJsonNull()) {
            return elem.getAsString();
        }
        return null;
    }
}
