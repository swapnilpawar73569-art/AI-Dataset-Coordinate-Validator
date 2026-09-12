package com.oopsproject.validator;

import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;

import com.oopsproject.validator.service.ValidationEngine;
import com.oopsproject.validator.util.ReportGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTest {

    @Test
    @DisplayName("Test exporting reports to TXT, CSV, and JSON formats")
    public void testReportExporter(@TempDir Path tempDir) throws Exception {
        ValidationEngine engine = ValidationEngine.createDefaultEngine();
        List<Coordinate> dataset = Arrays.asList(
                new Coordinate(1, "Valid", 28.6139, 77.2090),
                new Coordinate(2, "Invalid", 100.0, 45.0)
        );
        ValidationReport report = engine.validate(dataset);

        Path txtPath = tempDir.resolve("report.txt");
        Path csvPath = tempDir.resolve("report.csv");
        Path jsonPath = tempDir.resolve("report.json");

        ReportGenerator.exportReport(report, txtPath.toString());
        ReportGenerator.exportReport(report, csvPath.toString());
        ReportGenerator.exportReport(report, jsonPath.toString());

        assertTrue(new File(txtPath.toString()).exists());
        assertTrue(new File(csvPath.toString()).exists());
        assertTrue(new File(jsonPath.toString()).exists());

        assertTrue(new File(txtPath.toString()).length() > 0);
        assertTrue(new File(csvPath.toString()).length() > 0);
        assertTrue(new File(jsonPath.toString()).length() > 0);
    }
}
