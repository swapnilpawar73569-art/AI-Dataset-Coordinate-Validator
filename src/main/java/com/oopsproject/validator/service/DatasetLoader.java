package com.oopsproject.validator.service;

import com.oopsproject.validator.model.Coordinate;

import java.util.List;

/**
 * Responsible for loading datasets (CSV/JSON) from disk and converting
 * rows into Coordinate objects.
 *
 * Owner: [Teammate name here]
 */
public class DatasetLoader {

    public List<Coordinate> loadFromCsv(String filePath) {
        // TODO: use Apache Commons CSV to parse the file
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<Coordinate> loadFromJson(String filePath) {
        // TODO: use Gson to parse the file
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
