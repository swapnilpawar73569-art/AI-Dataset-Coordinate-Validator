package com.oopsproject.validator.util;

import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Pure Java JSON report exporter without external libraries.
 */
public class JsonReportExporter implements ReportExporter {

    @Override
    public String getSupportedExtension() {
        return ".json";
    }

    @Override
    public void export(ValidationReport report, File targetFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"totalRows\": ").append(report.getTotalCount()).append(",\n");
        sb.append("  \"validRows\": ").append(report.getValidCount()).append(",\n");
        sb.append("  \"invalidRows\": ").append(report.getInvalidCount()).append(",\n");
        sb.append("  \"results\": [\n");

        List<ValidationResult> results = report.getResults();
        for (int i = 0; i < results.size(); i++) {
            ValidationResult res = results.get(i);
            Coordinate c = res.getCoordinate();
            sb.append("    {\n");
            sb.append("      \"rowNumber\": ").append(c.getRowNumber()).append(",\n");
            sb.append("      \"label\": \"").append(escapeJson(c.getLabel())).append("\",\n");
            sb.append("      \"rawLatitude\": \"").append(escapeJson(c.getRawLatitude())).append("\",\n");
            sb.append("      \"rawLongitude\": \"").append(escapeJson(c.getRawLongitude())).append("\",\n");
            sb.append("      \"status\": \"").append(res.isValid() ? "VALID" : "INVALID").append("\",\n");
            sb.append("      \"failureReasons\": [");

            List<String> errors = res.getErrorMessages();
            for (int j = 0; j < errors.size(); j++) {
                sb.append("\"").append(escapeJson(errors.get(j))).append("\"");
                if (j < errors.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]\n");
            sb.append("    }").append(i < results.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.print(sb.toString());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
