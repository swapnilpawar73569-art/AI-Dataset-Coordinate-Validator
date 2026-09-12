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
}
