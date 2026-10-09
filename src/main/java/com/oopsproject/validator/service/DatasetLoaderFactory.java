package com.oopsproject.validator.service;

import com.oopsproject.validator.exception.UnsupportedFormatException;

/**
 * Factory class for creating DatasetLoader instances based on file format.
 */
public class DatasetLoaderFactory {

    /**
     * Factory method to obtain appropriate loader based on file path extension.
     *
     * @param filePath path to dataset file
     * @return DatasetLoader instance
     * @throws UnsupportedFormatException if format is not supported
     */
    public static DatasetLoader getLoader(String filePath) throws UnsupportedFormatException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new UnsupportedFormatException("File path cannot be null or empty.");
        }

        String lowerPath = filePath.toLowerCase().trim();
        if (lowerPath.endsWith(".csv")) {
            return new CsvDatasetLoader();
        } else if (lowerPath.endsWith(".json")) {
            return new JsonDatasetLoader();
        } else {
            throw new UnsupportedFormatException("Unsupported dataset format for file: " + filePath +
                    ". Only .csv and .json are supported.");
        }
    }

    /**
     * Factory method to obtain appropriate loader based on format name (csv or json).
     *
     * @param format format identifier ("csv" or "json")
     * @return DatasetLoader instance
     * @throws UnsupportedFormatException if format is not supported
     */
    public static DatasetLoader getLoaderForFormat(String format) throws UnsupportedFormatException {
        if (format == null || format.trim().isEmpty()) {
            throw new UnsupportedFormatException("Format cannot be null or empty.");
        }
        String f = format.toLowerCase().trim();
        if (f.equals("csv") || f.endsWith(".csv")) {
            return new CsvDatasetLoader();
        } else if (f.equals("json") || f.endsWith(".json")) {
            return new JsonDatasetLoader();
        } else {
            throw new UnsupportedFormatException("Unsupported dataset format: " + format + ". Only 'csv' and 'json' are supported.");
        }
    }
}
