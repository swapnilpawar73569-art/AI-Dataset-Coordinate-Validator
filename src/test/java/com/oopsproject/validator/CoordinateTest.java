package com.oopsproject.validator;

import com.oopsproject.validator.model.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateTest {

    @Test
    @DisplayName("Test Coordinate constructor with valid double values")
    public void testValidCoordinateConstructor() {
        Coordinate coord = new Coordinate(1, "Delhi", 28.6139, 77.2090);

        assertEquals(1, coord.getRowNumber());
        assertEquals("Delhi", coord.getLabel());
        assertEquals(28.6139, coord.getLatitude());
        assertEquals(77.2090, coord.getLongitude());
        assertFalse(coord.hasMissingValue());
        assertFalse(coord.hasFormatError());
        assertTrue(coord.isParseable());
    }

    @Test
    @DisplayName("Test Coordinate with missing values")
    public void testMissingCoordinateFlags() {
        Coordinate coord = new Coordinate(2, "MissingLat", null, 77.2090);

        assertTrue(coord.isLatitudeMissing());
        assertFalse(coord.isLongitudeMissing());
        assertTrue(coord.hasMissingValue());
        assertFalse(coord.isParseable());
    }

    @Test
    @DisplayName("Test Coordinate equality for duplicate detection")
    public void testCoordinateEquals() {
        Coordinate coord1 = new Coordinate(1, "A", 28.6139, 77.2090);
        Coordinate coord2 = new Coordinate(2, "B", 28.6139, 77.2090);
        Coordinate coord3 = new Coordinate(3, "C", 19.0760, 72.8777);

        assertEquals(coord1, coord2);
        assertNotEquals(coord1, coord3);
    }
}
