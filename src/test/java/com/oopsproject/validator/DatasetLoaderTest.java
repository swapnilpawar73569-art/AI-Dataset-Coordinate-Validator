package com.oopsproject.validator;

import com.oopsproject.validator.exception.DatasetReadException;
import com.oopsproject.validator.exception.InvalidDatasetException;
import com.oopsproject.validator.exception.UnsupportedFormatException;
import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.service.CsvDatasetLoader;
import com.oopsproject.validator.service.DatasetLoader;
import com.oopsproject.validator.service.DatasetLoaderFactory;
import com.oopsproject.validator.service.JsonDatasetLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatasetLoaderTest {

    @Test
    @DisplayName("Test CSV DatasetLoader with sample CSV file")
    public void testCsvDatasetLoader() throws InvalidDatasetException {
        DatasetLoader loader = new CsvDatasetLoader();
        List<Coordinate> coords = loader.load("data/sample_coordinates.csv");

        assertNotNull(coords);
        assertFalse(coords.isEmpty());
        assertEquals(6, coords.size());

        // Row 1: Point_A, 28.6139, 77.2090 (Valid)
        Coordinate pointA = coords.get(0);
        assertEquals("Point_A", pointA.getLabel());
        assertEquals(28.6139, pointA.getLatitude());

        // Row 6: Point_F, missing latitude
        Coordinate pointF = coords.get(5);
        assertTrue(pointF.isLatitudeMissing());
    }

    @Test
    @DisplayName("Test JSON DatasetLoader with sample JSON file")
    public void testJsonDatasetLoader() throws InvalidDatasetException {
        DatasetLoader loader = new JsonDatasetLoader();
        List<Coordinate> coords = loader.load("data/sample_coordinates.json");

        assertNotNull(coords);
        assertEquals(7, coords.size());
    }

    @Test
    @DisplayName("Test DatasetLoaderFactory for file extensions")
    public void testDatasetLoaderFactory() throws UnsupportedFormatException {
        DatasetLoader csvLoader = DatasetLoaderFactory.getLoader("data/sample_coordinates.csv");
        assertTrue(csvLoader instanceof CsvDatasetLoader);

        DatasetLoader jsonLoader = DatasetLoaderFactory.getLoader("data/sample_coordinates.json");
        assertTrue(jsonLoader instanceof JsonDatasetLoader);

        assertThrows(UnsupportedFormatException.class, () -> {
            DatasetLoaderFactory.getLoader("dataset.xml");
        });
    }

    @Test
    @DisplayName("Test Exception thrown when file does not exist")
    public void testNonExistentFileThrowsException() {
        DatasetLoader loader = new CsvDatasetLoader();
        assertThrows(DatasetReadException.class, () -> {
            loader.load("non_existent_file.csv");
        });
    }
}
