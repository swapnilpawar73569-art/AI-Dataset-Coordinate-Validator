package com.oopsproject.validator;

import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;
import com.oopsproject.validator.service.*;
import com.oopsproject.validator.util.ReportGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java Test Runner that verifies the entire system with zero external dependencies.
 * Run directly with: java -cp bin com.oopsproject.validator.TestRunner
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING PURE JAVA TEST SUITE FOR OOP PROJECT");
        System.out.println("=================================================");

        testCoordinateModel();
        testRangeValidator();
        testFormatValidator();
        testDuplicateValidator();
        testBoundingBoxValidator();
        testOutlierValidator();
        testCsvLoader();
        testJsonLoader();
        testValidationEngine();
        testReportExport();

        System.out.println("=================================================");
        System.out.println("  RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println(" [PASS] " + testName);
            passed++;
        } else {
            System.err.println(" [FAIL] " + testName);
            failed++;
        }
    }

    private static void testCoordinateModel() {
        Coordinate c = new Coordinate(1, "TestPoint", 28.6139, 77.2090);
        assertTrue("Coordinate encapsulation & getters",
                c.getRowNumber() == 1 &&
                c.getLabel().equals("TestPoint") &&
                c.getLatitude() == 28.6139 &&
                c.getLongitude() == 77.2090 &&
                c.isParseable());
    }

    private static void testRangeValidator() {
        RangeValidator v = new RangeValidator();
        Coordinate valid = new Coordinate(1, "Valid", 28.0, 77.0);
        Coordinate invalidLat = new Coordinate(2, "BadLat", 95.0, 77.0);
        Coordinate invalidLon = new Coordinate(3, "BadLon", 28.0, 195.0);

        assertTrue("RangeValidator accepts valid coordinates", v.isValid(valid, List.of(valid)));
        assertTrue("RangeValidator rejects latitude > 90", !v.isValid(invalidLat, List.of(invalidLat)));
        assertTrue("RangeValidator rejects longitude > 180", !v.isValid(invalidLon, List.of(invalidLon)));
    }

    private static void testFormatValidator() {
        FormatValidator v = new FormatValidator();
        Coordinate valid = new Coordinate(1, "Valid", 10.0, 20.0);
        Coordinate missing = new Coordinate(2, "MissingLat", "", "20.0", null, 20.0, true, false, false, false);
        Coordinate corrupt = new Coordinate(3, "Corrupt", "10.0", "BAD", 10.0, null, false, false, false, true);

        assertTrue("FormatValidator accepts clean numbers", v.isValid(valid, List.of(valid)));
        assertTrue("FormatValidator detects missing latitude", !v.isValid(missing, List.of(missing)));
        assertTrue("FormatValidator detects corrupt number string", !v.isValid(corrupt, List.of(corrupt)));
    }

    private static void testDuplicateValidator() {
        DuplicateValidator v = new DuplicateValidator();
        Coordinate c1 = new Coordinate(1, "PointA", 28.6139, 77.2090);
        Coordinate c2 = new Coordinate(2, "PointB", 19.0760, 72.8777);
        Coordinate c3 = new Coordinate(3, "PointC_DuplicateOfA", 28.6139, 77.2090);
        List<Coordinate> list = List.of(c1, c2, c3);

        assertTrue("DuplicateValidator passes unique coordinate", v.isValid(c2, list));
        assertTrue("DuplicateValidator flags duplicate coordinate", !v.isValid(c1, list) && !v.isValid(c3, list));
    }

    private static void testBoundingBoxValidator() {
        BoundingBoxValidator v = new BoundingBoxValidator("India", 6.0, 37.5, 68.0, 97.5);
        Coordinate inside = new Coordinate(1, "Delhi", 28.6139, 77.2090);
        Coordinate outside = new Coordinate(2, "London", 51.5074, -0.1278);

        assertTrue("BoundingBoxValidator allows point inside box", v.isValid(inside, List.of(inside)));
        assertTrue("BoundingBoxValidator rejects point outside box", !v.isValid(outside, List.of(outside)));
    }

    private static void testOutlierValidator() {
        OutlierValidator v = new OutlierValidator(2.0);
        List<Coordinate> cluster = new ArrayList<>();
        // Cluster in Delhi
        for (int i = 0; i < 10; i++) {
            cluster.add(new Coordinate(i + 1, "Cluster" + i, 28.6 + (i * 0.01), 77.2 + (i * 0.01)));
        }
        Coordinate outlier = new Coordinate(99, "Antarctica", -80.0, 100.0);
        cluster.add(outlier);

        assertTrue("OutlierValidator flags extreme geographic outlier", !v.isValid(outlier, cluster));
    }

    private static void testCsvLoader() {
        try {
            CsvDatasetLoader loader = new CsvDatasetLoader();
            List<Coordinate> coords = loader.load("data/sample_coordinates.csv");
            assertTrue("CsvDatasetLoader loads 6 rows from sample CSV", coords != null && coords.size() == 6);
            assertTrue("CsvDatasetLoader parses first row correctly", coords.get(0).getLabel().equals("Point_A"));
        } catch (Exception e) {
            assertTrue("CsvDatasetLoader threw exception: " + e.getMessage(), false);
        }
    }

    private static void testJsonLoader() {
        try {
            JsonDatasetLoader loader = new JsonDatasetLoader();
            List<Coordinate> coords = loader.load("data/sample_coordinates.json");
            assertTrue("JsonDatasetLoader loads 7 rows from sample JSON", coords != null && coords.size() == 7);
        } catch (Exception e) {
            assertTrue("JsonDatasetLoader threw exception: " + e.getMessage(), false);
        }
    }

    private static void testValidationEngine() {
        try {
            ValidationEngine engine = ValidationEngine.createDefaultEngine();
            CsvDatasetLoader loader = new CsvDatasetLoader();
            List<Coordinate> coords = loader.load("data/sample_coordinates.csv");
            ValidationReport report = engine.validate(coords);

            assertTrue("ValidationEngine produces report for 6 rows", report.getTotalCount() == 6);
            assertTrue("ValidationEngine identifies 2 valid rows", report.getValidCount() == 2);
            assertTrue("ValidationEngine identifies 4 invalid rows", report.getInvalidCount() == 4);
        } catch (Exception e) {
            assertTrue("ValidationEngine threw exception: " + e.getMessage(), false);
        }
    }

    private static void testReportExport() {
        try {
            ValidationEngine engine = ValidationEngine.createDefaultEngine();
            List<Coordinate> coords = List.of(new Coordinate(1, "Test", 28.0, 77.0));
            ValidationReport report = engine.validate(coords);

            File txtFile = File.createTempFile("test_report", ".txt");
            File csvFile = File.createTempFile("test_report", ".csv");
            File jsonFile = File.createTempFile("test_report", ".json");

            ReportGenerator.exportReport(report, txtFile.getAbsolutePath());
            ReportGenerator.exportReport(report, csvFile.getAbsolutePath());
            ReportGenerator.exportReport(report, jsonFile.getAbsolutePath());

            assertTrue("ReportGenerator exports TXT file", txtFile.exists() && txtFile.length() > 0);
            assertTrue("ReportGenerator exports CSV file", csvFile.exists() && csvFile.length() > 0);
            assertTrue("ReportGenerator exports JSON file", jsonFile.exists() && jsonFile.length() > 0);

            txtFile.delete();
            csvFile.delete();
            jsonFile.delete();
        } catch (Exception e) {
            assertTrue("ReportGenerator threw exception: " + e.getMessage(), false);
        }
    }
}
