package com.oopsproject.validator.ui;

import com.oopsproject.validator.exception.InvalidDatasetException;
import com.oopsproject.validator.exception.ValidationException;
import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.service.DatasetLoader;
import com.oopsproject.validator.service.DatasetLoaderFactory;
import com.oopsproject.validator.service.ValidationEngine;
import com.oopsproject.validator.util.ReportGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console UI for the AI Dataset Coordinate Validator.
 */
public class ConsoleMenu {

    private final Scanner scanner;
    private final ValidationEngine validationEngine;
    private List<Coordinate> currentDataset;
    private ValidationReport currentReport;
    private String currentFilePath;

    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        this.validationEngine = ValidationEngine.createDefaultEngine();
    }

    public void start() {
        System.out.println("=================================================");
        System.out.println("  AI Dataset Coordinate Validator (Console UI)");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice (1-5): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    loadDatasetMenu();
                    break;
                case "2":
                    runValidationMenu();
                    break;
                case "3":
                    viewReportMenu();
                    break;
                case "4":
                    exportReportMenu();
                    break;
                case "5":
                    running = false;
                    System.out.println("Exiting Coordinate Validator. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please select 1 through 5.\n");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. Load Dataset File (CSV or JSON)");
        System.out.println("2. Run Validation Suite");
        System.out.println("3. View Report Summary");
        System.out.println("4. Export Report to File");
        System.out.println("5. Exit");
        if (currentFilePath != null) {
            System.out.println("Current File: " + currentFilePath + " (" + 
                    (currentDataset != null ? currentDataset.size() + " rows loaded" : "not validated") + ")");
        }
        System.out.println("-------------------------------------------");
    }

    public void loadDataset(String path) {
        try {
            DatasetLoader loader = DatasetLoaderFactory.getLoader(path);
            this.currentDataset = loader.load(path);
            this.currentFilePath = path;
            this.currentReport = null; // reset previous report
            System.out.println("SUCCESS: Loaded " + currentDataset.size() + " coordinate rows from: " + path);
        } catch (InvalidDatasetException e) {
            System.err.println("ERROR loading dataset: " + e.getMessage());
        }
    }

    private void loadDatasetMenu() {
        System.out.print("Enter file path (e.g. data/sample_coordinates.csv or .json): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("File path cannot be empty.");
            return;
        }
        loadDataset(path);
    }

    public void runValidation() {
        if (currentDataset == null || currentDataset.isEmpty()) {
            System.out.println("No dataset loaded. Please load a file first.");
            return;
        }

        try {
            this.currentReport = validationEngine.validate(currentDataset);
            System.out.println("\nSUCCESS: Validation executed on " + currentReport.getTotalCount() + " rows.");
            System.out.println("Result: " + currentReport.getValidCount() + " VALID, " + 
                    currentReport.getInvalidCount() + " INVALID.");
        } catch (ValidationException e) {
            System.err.println("ERROR during validation: " + e.getMessage());
        }
    }

    private void runValidationMenu() {
        runValidation();
    }

    private void viewReportMenu() {
        if (currentReport == null) {
            if (currentDataset == null) {
                System.out.println("No dataset loaded. Please load a file first.");
            } else {
                System.out.println("Validation has not been executed yet. Run option 2 first.");
            }
            return;
        }

        System.out.println("\n" + ReportGenerator.generateConsoleReport(currentReport));
    }

    private void exportReportMenu() {
        if (currentReport == null) {
            System.out.println("No validation report available to export. Load a file and run validation first.");
            return;
        }

        System.out.print("Enter target export file path (e.g. data/report.txt, report.csv, or report.json): ");
        String exportPath = scanner.nextLine().trim();
        if (exportPath.isEmpty()) {
            System.out.println("Export path cannot be empty.");
            return;
        }

        try {
            ReportGenerator.exportReport(currentReport, exportPath);
            System.out.println("SUCCESS: Report exported to: " + exportPath);
        } catch (IOException e) {
            System.err.println("ERROR exporting report: " + e.getMessage());
        }
    }
}
