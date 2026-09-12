package com.oopsproject.validator.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Handles formatting and exporting dataset validation reports to Console, TXT, CSV, and JSON formats.
 */
public class ReportGenerator {

    /**
     * Formats validation report into human-readable text output.
     */
    public static String generateConsoleReport(ValidationReport report) {
        if (report == null) return "No report data available.";
        return report.toString();
    }

    /**
     * Exports validation report to file in specified format (.txt, .csv, .json).
     *
     * @param report ValidationReport instance
     * @param exportFilePath Target export file path
     * @throws IOException if export fails
     */
    public static void exportReport(ValidationReport report, String exportFilePath) throws IOException {
        if (report == null || exportFilePath == null || exportFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Report and export file path must not be null or empty.");
        }

        File file = new File(exportFilePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        String lowerPath = exportFilePath.toLowerCase().trim();
        if (lowerPath.endsWith(".csv")) {
            exportToCsv(report, file);
        } else if (lowerPath.endsWith(".json")) {
            exportToJson(report, file);
        } else {
            // Default to plain text format
            exportToTxt(report, file);
        }
    }

    private static void exportToTxt(ValidationReport report, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.println(report.toString());
        }
    }

    private static void exportToCsv(ValidationReport report, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.println("RowNumber,Label,Latitude,Longitude,Status,FailureReasons");
            for (ValidationResult res : report.getResults()) {
                var c = res.getCoordinate();
                String status = res.isValid() ? "VALID" : "INVALID";
                String reasons = res.isValid() ? "" : res.getFormattedErrors().replace("\"", "\"\"");
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        c.getRowNumber(),
                        c.getLabel().replace("\"", "\"\""),
                        c.isLatitudeMissing() ? "MISSING" : c.getRawLatitude(),
                        c.isLongitudeMissing() ? "MISSING" : c.getRawLongitude(),
                        status,
                        reasons);
            }
        }
    }

    private static void exportToJson(ValidationReport report, File file) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("totalRows", report.getTotalCount());
        root.addProperty("validRows", report.getValidCount());
        root.addProperty("invalidRows", report.getInvalidCount());

        JsonArray resultsArray = new JsonArray();
        for (ValidationResult res : report.getResults()) {
            JsonObject item = new JsonObject();
            var c = res.getCoordinate();
            item.addProperty("rowNumber", c.getRowNumber());
            item.addProperty("label", c.getLabel());
            item.addProperty("rawLatitude", c.getRawLatitude());
            item.addProperty("rawLongitude", c.getRawLongitude());
            item.addProperty("status", res.isValid() ? "VALID" : "INVALID");
            
            JsonArray errors = new JsonArray();
            for (String err : res.getErrorMessages()) {
                errors.add(err);
            }
            item.add("failureReasons", errors);
            resultsArray.add(item);
        }
        root.add("results", resultsArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.println(gson.toJson(root));
        }
    }
}
