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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Modern Dashboard GUI featuring KPI Metric Cards, real-time filtering & search,
 * detailed row inspection, and multi-format report exports (.html, .csv, .json, .txt).
 */
public class ValidatorGUI extends JFrame {

    private final ValidationEngine validationEngine;
    private List<Coordinate> currentDataset;
    private ValidationReport currentReport;
    private String currentFilePath;

    private JTextField filePathField;
    private JButton runButton;
    private JButton exportButton;

    // KPI Metric Cards Labels
    private JLabel totalMetricLabel;
    private JLabel validMetricLabel;
    private JLabel invalidMetricLabel;
    private JLabel passRateMetricLabel;

    // Filter & Search Controls
    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;

    // Table & Models
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Detail Inspection Panel
    private JTextArea inspectionTextArea;

    public ValidatorGUI() {
        this.validationEngine = ValidationEngine.createDefaultEngine();
        initUI();
    }

    private void initUI() {
        setTitle("AI Dataset Coordinate Validator - Modern Dashboard GUI");
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Top Panel: File Input + KPI Metric Cards
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filePanel.setBorder(BorderFactory.createTitledBorder("Dataset Controls"));

        filePathField = new JTextField(38);
        filePathField.setEditable(false);

        JButton browseButton = new JButton("Browse Dataset...");
        browseButton.addActionListener(e -> chooseFile());

        runButton = new JButton("Run Validation");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> runValidation());

        exportButton = new JButton("Export Report...");
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportReport());

        filePanel.add(new JLabel("File:"));
        filePanel.add(filePathField);
        filePanel.add(browseButton);
        filePanel.add(runButton);
        filePanel.add(exportButton);

        northPanel.add(filePanel);

        // KPI Dashboard Panel (4 Metric Cards)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        kpiPanel.add(createKpiCard("TOTAL ROWS", "0", new Color(49, 130, 206), label -> totalMetricLabel = label));
        kpiPanel.add(createKpiCard("VALID ROWS", "0", new Color(56, 161, 105), label -> validMetricLabel = label));
        kpiPanel.add(createKpiCard("INVALID ROWS", "0", new Color(229, 62, 62), label -> invalidMetricLabel = label));
        kpiPanel.add(createKpiCard("PASS RATE", "0.0%", new Color(128, 90, 213), label -> passRateMetricLabel = label));

        northPanel.add(kpiPanel);
        add(northPanel, BorderLayout.NORTH);

        // 2. Center Panel: Filter Bar + Results Table
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("Filter Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"All Rows", "Valid Only", "Invalid Only"});
        statusFilterCombo.addActionListener(e -> applyTableFilters());
        filterPanel.add(statusFilterCombo);

        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyTableFilters(); }
            public void removeUpdate(DocumentEvent e) { applyTableFilters(); }
            public void changedUpdate(DocumentEvent e) { applyTableFilters(); }
        });
        filterPanel.add(searchField);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columnNames = {"Row #", "Label", "Latitude", "Longitude", "Status", "Failure Reason(s)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableSorter = new TableRowSorter<>(tableModel);
        resultsTable = new JTable(tableModel);
        resultsTable.setRowSorter(tableSorter);
        resultsTable.setRowHeight(24);

        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
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

        // Row Selection listener for detailed inspection
        resultsTable.getSelectionModel().addListSelectionListener(e -> updateInspectionDetail());

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dataset Results Matrix"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Panel: Interactive Inspection Detail Card
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Selected Row Detail Inspection"));
        bottomPanel.setPreferredSize(new Dimension(0, 100));

        inspectionTextArea = new JTextArea("Select a row in the table above to view detailed validation rule results.");
        inspectionTextArea.setEditable(false);
        inspectionTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inspectionTextArea.setBackground(new Color(245, 247, 250));

        bottomPanel.add(new JScrollPane(inspectionTextArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createKpiCard(String title, String initialValue, Color bg, java.util.function.Consumer<JLabel> labelConsumer) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 11f));

        JLabel valLabel = new JLabel(initialValue);
        valLabel.setForeground(Color.WHITE);
        valLabel.setFont(valLabel.getFont().deriveFont(Font.BOLD, 22f));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);

        labelConsumer.accept(valLabel);
        return card;
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
                resetKpis();
                inspectionTextArea.setText("Dataset loaded (" + currentDataset.size() + " rows). Click 'Run Validation' to analyze.");
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

            updateKpis(currentReport);
            exportButton.setEnabled(true);
            inspectionTextArea.setText("Validation execution complete. Select a row to inspect failure details.");
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, "Validation execution failed:\n" + ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetKpis() {
        totalMetricLabel.setText("0");
        validMetricLabel.setText("0");
        invalidMetricLabel.setText("0");
        passRateMetricLabel.setText("0.0%");
    }

    private void updateKpis(ValidationReport report) {
        totalMetricLabel.setText(String.valueOf(report.getTotalCount()));
        validMetricLabel.setText(String.valueOf(report.getValidCount()));
        invalidMetricLabel.setText(String.valueOf(report.getInvalidCount()));
        double rate = report.getTotalCount() > 0 ? (report.getValidCount() * 100.0 / report.getTotalCount()) : 0.0;
        passRateMetricLabel.setText(String.format("%.1f%%", rate));
    }

    private void applyTableFilters() {
        if (tableSorter == null) return;

        String selectedFilter = (String) statusFilterCombo.getSelectedItem();
        String searchText = searchField.getText().trim();

        RowFilter<DefaultTableModel, Object> statusFilter = null;
        if ("Valid Only".equals(selectedFilter)) {
            statusFilter = RowFilter.regexFilter("^VALID$", 4);
        } else if ("Invalid Only".equals(selectedFilter)) {
            statusFilter = RowFilter.regexFilter("^INVALID$", 4);
        }

        RowFilter<DefaultTableModel, Object> textFilter = null;
        if (!searchText.isEmpty()) {
            textFilter = RowFilter.regexFilter("(?i)" + searchText);
        }

        if (statusFilter != null && textFilter != null) {
            tableSorter.setRowFilter(RowFilter.andFilter(List.of(statusFilter, textFilter)));
        } else if (statusFilter != null) {
            tableSorter.setRowFilter(statusFilter);
        } else if (textFilter != null) {
            tableSorter.setRowFilter(textFilter);
        } else {
            tableSorter.setRowFilter(null);
        }
    }

    private void updateInspectionDetail() {
        int viewRow = resultsTable.getSelectedRow();
        if (viewRow < 0 || currentReport == null) {
            return;
        }

        int modelRow = resultsTable.convertRowIndexToModel(viewRow);
        ValidationResult result = currentReport.getResults().get(modelRow);
        Coordinate c = result.getCoordinate();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ROW #%d | Label: '%s' | Lat: %s | Lon: %s%n",
                c.getRowNumber(), c.getLabel(),
                c.isLatitudeMissing() ? "<MISSING>" : c.getRawLatitude(),
                c.isLongitudeMissing() ? "<MISSING>" : c.getRawLongitude()));
        sb.append("STATUS: ").append(result.isValid() ? "VALID (All Rules Passed)" : "INVALID").append("\n");

        if (!result.isValid()) {
            sb.append("VIOLATION DETAILS:\n");
            for (String err : result.getErrorMessages()) {
                sb.append(" - ").append(err).append("\n");
            }
        }

        inspectionTextArea.setText(sb.toString());
    }

    private void exportReport() {
        if (currentReport == null) return;

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Export Validation Report");
        chooser.setSelectedFile(new File("validation_report.html"));

        chooser.addChoosableFileFilter(new FileNameExtensionFilter("HTML Report (*.html)", "html"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV File (*.csv)", "csv"));
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
