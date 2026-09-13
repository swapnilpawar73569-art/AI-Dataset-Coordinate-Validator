package com.oopsproject.validator.util;

import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HtmlReportExporter implements ReportExporter {
    @Override
    public String getSupportedExtension() {
        return ".html";
    }

    @Override
    public void export(ValidationReport report, File targetFile) throws IOException {
        double passRate = report.getTotalCount() > 0 ? (report.getValidCount() * 100.0 / report.getTotalCount()) : 0.0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head><meta charset='UTF-8'><title>AI Coordinate Dataset Validation Report</title>");
            writer.println("<style>");
            writer.println("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 30px; background: #f8f9fa; color: #333; }");
            writer.println(".container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }");
            writer.println("h1 { color: #1a202c; border-bottom: 2px solid #e2e8f0; padding-bottom: 12px; }");
            writer.println(".stats { display: flex; gap: 20px; margin: 24px 0; }");
            writer.println(".card { flex: 1; padding: 20px; border-radius: 8px; text-align: center; color: white; font-weight: bold; }");
            writer.println(".card.total { background: #3182ce; }");
            writer.println(".card.valid { background: #38a169; }");
            writer.println(".card.invalid { background: #e53e3e; }");
            writer.println(".card.rate { background: #805ad5; }");
            writer.println(".card .val { font-size: 28px; margin-top: 8px; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            writer.println("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e2e8f0; }");
            writer.println("th { background: #edf2f7; color: #4a5568; }");
            writer.println(".badge { padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 12px; text-transform: uppercase; }");
            writer.println(".badge.valid { background: #c6f6d5; color: #22543d; }");
            writer.println(".badge.invalid { background: #fed7d7; color: #742a2a; }");
            writer.println("</style></head><body>");

            writer.println("<div class='container'>");
            writer.println("<h1>📍 AI Dataset Coordinate Validation Report</h1>");

            writer.println("<div class='stats'>");
            writer.printf("<div class='card total'>Total Rows<div class='val'>%d</div></div>%n", report.getTotalCount());
            writer.printf("<div class='card valid'>Valid Rows<div class='val'>%d</div></div>%n", report.getValidCount());
            writer.printf("<div class='card invalid'>Invalid Rows<div class='val'>%d</div></div>%n", report.getInvalidCount());
            writer.printf("<div class='card rate'>Pass Rate<div class='val'>%.1f%%</div></div>%n", passRate);
            writer.println("</div>");

            writer.println("<table>");
            writer.println("<thead><tr><th>Row #</th><th>Label</th><th>Latitude</th><th>Longitude</th><th>Status</th><th>Failure Reasons</th></tr></thead>");
            writer.println("<tbody>");

            for (ValidationResult res : report.getResults()) {
                var c = res.getCoordinate();
                String statusClass = res.isValid() ? "valid" : "invalid";
                String statusText = res.isValid() ? "VALID" : "INVALID";
                String latStr = c.isLatitudeMissing() ? "&lt;MISSING&gt;" : c.getRawLatitude();
                String lonStr = c.isLongitudeMissing() ? "&lt;MISSING&gt;" : c.getRawLongitude();
                String reasons = res.isValid() ? "-" : res.getFormattedErrors();

                writer.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td><span class='badge %s'>%s</span></td><td>%s</td></tr>%n",
                        c.getRowNumber(), c.getLabel(), latStr, lonStr, statusClass, statusText, reasons);
            }

            writer.println("</tbody></table>");
            writer.println("</div></body></html>");
        }
    }
}
