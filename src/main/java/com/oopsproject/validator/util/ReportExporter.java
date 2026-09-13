package com.oopsproject.validator.util;

import com.oopsproject.validator.model.ValidationReport;

import java.io.File;
import java.io.IOException;

/**
 * Strategy interface contract for exporting validation reports to various file formats (Polymorphism & Strategy Pattern).
 */
public interface ReportExporter {
    /**
     * Exports validation report to the target file.
     */
    void export(ValidationReport report, File targetFile) throws IOException;

    /**
     * @return supported format file extension (e.g. ".txt", ".csv", ".json", ".html")
     */
    String getSupportedExtension();
}
