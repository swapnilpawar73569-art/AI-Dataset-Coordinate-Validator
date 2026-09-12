package com.oopsproject.validator.ui;

import com.oopsproject.validator.exception.InvalidDatasetException;
import com.oopsproject.validator.exception.ValidationException;
import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;
import com.oopsproject.validator.service.DatasetLoader;
import com.oopsproject.validator.service.DatasetLoaderFactory;
import com.oopsproject.validator.service.ValidationEngine;
import com.oopsproject.validator.util.ReportGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Phase 2 Graphical User Interface built with Swing.
 */
public class ValidatorGUI extends JFrame {

    private final ValidationEngine validationEngine;
    private List<Coordinate> currentDataset;
    private ValidationReport currentReport;
    private String currentFilePath;

    private JTextField filePathField;
    private JLabel statusSummaryLabel;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton runButton;
    private JButton exportButton;

    public ValidatorGUI() {
        this.validationEngine = ValidationEngine.createDefaultEngine();
        initUI();
    }

    private void initUI() {
        setTitle("AI Dataset Coordinate Validator - Swing GUI");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top Control Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Dataset Input"));

        filePathField = new JTextField(35);
        filePathField.setEditable(false);

        JButton browseButton = new JButton("Browse Dataset...");
        browseButton.addActionListener(e -> chooseFile());

        runButton = new JButton("Run Validation");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> runValidation());

        exportButton = new JButton("Export Report...");
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportReport());

        topPanel.add(new JLabel("File:"));
        topPanel.add(filePathField);
        topPanel.add(browseButton);
        topPanel.add(runButton);
        topPanel.add(exportButton);

        add(topPanel, BorderLayout.NORTH);

        // Center Table Panel
        String[] columnNames = {"Row #", "Label", "Latitude", "Longitude", "Status", "Failure Reason(s)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(24);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(450);

        // Highlight Status column cells
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("VALID".equals(value)) {
                    c.setForeground(new Color(0, 128, 0));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if ("INVALID".equals(value)) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Validation Results"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        statusSummaryLabel = new JLabel("Please select a CSV or JSON dataset file to begin.");
        statusSummaryLabel.setFont(statusSummaryLabel.getFont().deriveFont(Font.BOLD, 13f));
        bottomPanel.add(statusSummaryLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Select Coordinate Dataset File");
        chooser.setFileFilter(new FileNameExtensionFilter("Dataset Files (*.csv, *.json)", "csv", "json"));

        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            currentFilePath = selectedFile.getAbsolutePath();
            filePathField.setText(currentFilePath);

            try {
                DatasetLoader loader = DatasetLoaderFactory.getLoader(currentFilePath);
                currentDataset = loader.load(currentFilePath);
                runButton.setEnabled(true);
                exportButton.setEnabled(false);
                currentReport = null;
                tableModel.setRowCount(0);

                statusSummaryLabel.setText(String.format("Loaded %d rows from %s. Click 'Run Validation' to analyze.",
                        currentDataset.size(), selectedFile.getName()));
            } catch (InvalidDatasetException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load dataset:\n" + ex.getMessage(),
                        "Dataset Load Error", JOptionPane.ERROR_MESSAGE);
                runButton.setEnabled(false);
                exportButton.setEnabled(false);
            }
        }
    }

    private void runValidation() {
        if (currentDataset == null || currentDataset.isEmpty()) {
            return;
        }

        try {
            currentReport = validationEngine.validate(currentDataset);
            tableModel.setRowCount(0);

            for (ValidationResult res : currentReport.getResults()) {
                var c = res.getCoordinate();
                tableModel.addRow(new Object[]{
                        c.getRowNumber(),
                        c.getLabel(),
                        c.isLatitudeMissing() ? "<MISSING>" : c.getRawLatitude(),
                        c.isLongitudeMissing() ? "<MISSING>" : c.getRawLongitude(),
                        res.isValid() ? "VALID" : "INVALID",
                        res.getFormattedErrors()
                });
            }

            statusSummaryLabel.setText(String.format("Validation Complete! Total: %d | Valid: %d | Invalid: %d",
                    currentReport.getTotalCount(), currentReport.getValidCount(), currentReport.getInvalidCount()));

            exportButton.setEnabled(true);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, "Validation execution failed:\n" + ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReport() {
        if (currentReport == null) return;

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Export Validation Report");
        chooser.setSelectedFile(new File("validation_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV File (*.csv)", "csv"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON File (*.json)", "json"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));

        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            String exportPath = chooser.getSelectedFile().getAbsolutePath();
            try {
                ReportGenerator.exportReport(currentReport, exportPath);
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + exportPath,
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to export report:\n" + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
