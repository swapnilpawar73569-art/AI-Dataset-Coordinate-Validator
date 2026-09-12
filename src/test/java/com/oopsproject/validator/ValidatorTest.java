package com.oopsproject.validator;

import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {

    @Test
    @DisplayName("Test RangeValidator for boundary conditions")
    public void testRangeValidator() {
        RangeValidator validator = new RangeValidator();

        Coordinate valid = new Coordinate(1, "A", 45.0, 90.0);
        Coordinate invalidLat = new Coordinate(2, "B", 91.5, 45.0);
        Coordinate invalidLon = new Coordinate(3, "C", -13.0, 200.0);

        assertTrue(validator.isValid(valid, null));
        assertFalse(validator.isValid(invalidLat, null));
        assertFalse(validator.isValid(invalidLon, null));

        assertTrue(validator.getErrorMessage(invalidLat, null).contains("Latitude 91.5000 out of valid range"));
        assertTrue(validator.getErrorMessage(invalidLon, null).contains("Longitude 200.0000 out of valid range"));
    }

    @Test
    @DisplayName("Test FormatValidator for missing and non-numeric values")
    public void testFormatValidator() {
        FormatValidator validator = new FormatValidator();

        Coordinate valid = new Coordinate(1, "Valid", 28.6139, 77.2090);
        Coordinate missingLat = new Coordinate(2, "Missing", null, 77.2090);
        Coordinate invalidFormat = new Coordinate(3, "BadFormat", "12.34", "ABC", 12.34, null, false, false, false, true);

        assertTrue(validator.isValid(valid, null));
        assertFalse(validator.isValid(missingLat, null));
        assertFalse(validator.isValid(invalidFormat, null));

        assertTrue(validator.getErrorMessage(missingLat, null).contains("Latitude is missing"));
        assertTrue(validator.getErrorMessage(invalidFormat, null).contains("Longitude is not a valid number"));
    }

    @Test
    @DisplayName("Test DuplicateValidator for identical coordinate pairs")
    public void testDuplicateValidator() {
        DuplicateValidator validator = new DuplicateValidator();

        Coordinate p1 = new Coordinate(1, "Point1", 28.6139, 77.2090);
        Coordinate p2 = new Coordinate(2, "Point2", 19.0760, 72.8777);
        Coordinate p3 = new Coordinate(3, "Point3_Duplicate", 28.6139, 77.2090);

        List<Coordinate> dataset = Arrays.asList(p1, p2, p3);

        assertTrue(validator.isValid(p2, dataset));
        assertFalse(validator.isValid(p1, dataset));
        assertFalse(validator.isValid(p3, dataset));

        assertTrue(validator.getErrorMessage(p1, dataset).contains("Duplicate coordinate matches Row(s): [3]"));
    }

    @Test
    @DisplayName("Test OutlierValidator for statistical anomalies")
    public void testOutlierValidator() {
        OutlierValidator validator = new OutlierValidator(1.5); // 1.5 sigma threshold

        Coordinate p1 = new Coordinate(1, "Cluster1", 28.61, 77.20);
        Coordinate p2 = new Coordinate(2, "Cluster2", 28.62, 77.21);
        Coordinate p3 = new Coordinate(3, "Cluster3", 28.60, 77.19);
        Coordinate p4 = new Coordinate(4, "Cluster4", 28.61, 77.20);
        Coordinate p5 = new Coordinate(5, "Cluster5", 28.62, 77.22);
        Coordinate p6 = new Coordinate(6, "Outlier", -80.0, -170.0);

        List<Coordinate> dataset = Arrays.asList(p1, p2, p3, p4, p5, p6);

        assertTrue(validator.isValid(p1, dataset));
        assertFalse(validator.isValid(p6, dataset));
        assertTrue(validator.getErrorMessage(p6, dataset).contains("Statistical outlier detected"));
    }

    @Test
    @DisplayName("Test PrecisionValidator for decimal limits")
    public void testPrecisionValidator() {
        PrecisionValidator validator = new PrecisionValidator(4);

        Coordinate normal = new Coordinate(1, "Normal", "28.6139", "77.2090", 28.6139, 77.2090, false, false, false, false);
        Coordinate excessive = new Coordinate(2, "Excessive", "28.6139123456", "77.2090", 28.6139123456, 77.2090, false, false, false, false);

        assertTrue(validator.isValid(normal, null));
        assertFalse(validator.isValid(excessive, null));
        assertTrue(validator.getErrorMessage(excessive, null).contains("Latitude has excessive precision"));
    }
}
