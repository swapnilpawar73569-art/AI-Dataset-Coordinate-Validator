package com.oopsproject.validator.service;

import com.oopsproject.validator.exception.InvalidDatasetException;
import com.oopsproject.validator.model.Coordinate;

import java.util.List;

/**
 * Interface for loading coordinate datasets from disk.
 */
public interface DatasetLoader {
    /**
     * Loads coordinates from a file at the specified path.
     *
     * @param filePath path to the dataset file
     * @return List of Coordinate objects parsed from the file
     * @throws InvalidDatasetException if the file cannot be found, read, or parsed
     */
    List<Coordinate> load(String filePath) throws InvalidDatasetException;

    /**
     * Loads coordinates from an input character Reader stream.
     *
     * @param reader reader containing raw dataset text
     * @return List of Coordinate objects parsed from the stream
     * @throws InvalidDatasetException if reading or parsing fails
     */
    default List<Coordinate> load(java.io.Reader reader) throws InvalidDatasetException {
        throw new UnsupportedOperationException("Reader-based loading not supported by this loader.");
    }

    /**
     * Loads coordinates directly from a String.
     *
     * @param content raw dataset content
     * @return List of Coordinate objects parsed from the string
     * @throws InvalidDatasetException if parsing fails
     */
    default List<Coordinate> loadFromString(String content) throws InvalidDatasetException {
        if (content == null) {
            throw new InvalidDatasetException("Dataset content cannot be null.");
        }
        return load(new java.io.StringReader(content));
    }
}
