package com.oopsproject.validator.util;

import com.oopsproject.validator.model.ValidationReport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles formatting and exporting dataset validation reports using polymorphic ReportExporters.
 */
public class ReportGenerator {

    private static final Map<String, ReportExporter> EXPORTERS = new HashMap<>();

    static {
        registerExporter(new TxtReportExporter());
        registerExporter(new CsvReportExporter());
        registerExporter(new JsonReportExporter());
        registerExporter(new HtmlReportExporter());
    }

    public static void registerExporter(ReportExporter exporter) {
        if (exporter != null) {
            EXPORTERS.put(exporter.getSupportedExtension().toLowerCase(), exporter);
        }
    }

    /**
     * Formats validation report into human-readable text output.
     */
    public static String generateConsoleReport(ValidationReport report) {
        if (report == null) return "No report data available.";
        return report.toString();
    }

    /**
     * Polymorphically exports validation report to file in specified format (.txt, .csv, .json, .html).
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
        String ext = "";
        int dotIdx = lowerPath.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = lowerPath.substring(dotIdx);
        }

        ReportExporter exporter = EXPORTERS.get(ext);
        if (exporter == null) {
            // Default to plain text exporter if unknown format extension
            exporter = EXPORTERS.get(".txt");
        }

        exporter.export(report, file);
    }
}
