package com.oopsproject.validator.util;

import com.oopsproject.validator.model.ValidationReport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class TxtReportExporter implements ReportExporter {
    @Override
    public String getSupportedExtension() {
        return ".txt";
    }

    @Override
    public void export(ValidationReport report, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.println(report.toString());
        }
    }
}
