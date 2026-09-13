package com.oopsproject.validator.util;

import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CsvReportExporter implements ReportExporter {
    @Override
    public String getSupportedExtension() {
        return ".csv";
    }

    @Override
    public void export(ValidationReport report, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
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
}
