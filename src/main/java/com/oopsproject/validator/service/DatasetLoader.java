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
}
