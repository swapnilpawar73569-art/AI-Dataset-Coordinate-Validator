package com.oopsproject.validator;

import com.oopsproject.validator.ui.ConsoleMenu;
import com.oopsproject.validator.ui.ValidatorGUI;

import javax.swing.SwingUtilities;

/**
 * Main application entry point for the AI Dataset Coordinate Validator.
 * 
 * Simple to explain to Sir:
 * 1. Default execution: Launches the interactive pure Java Console Menu.
 * 2. Optional '--gui' flag: Launches the pure Java Desktop Swing GUI window.
 * 3. File argument: Passing a path (e.g. data/sample_coordinates.csv) automatically
 *    loads and validates the file.
 */
public class Main {

    public static void main(String[] args) {
        boolean launchGui = false;
        String autoLoadPath = null;

        for (String arg : args) {
            if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                launchGui = true;
            } else if (!arg.startsWith("-") && autoLoadPath == null) {
                autoLoadPath = arg;
            }
        }

        if (launchGui) {
            System.out.println("Starting AI Dataset Coordinate Validator (Desktop Swing GUI mode)...");
            SwingUtilities.invokeLater(() -> {
                ValidatorGUI gui = new ValidatorGUI();
                gui.setVisible(true);
            });
        } else {
            ConsoleMenu menu = new ConsoleMenu();
            if (autoLoadPath != null) {
                menu.loadDataset(autoLoadPath);
                menu.runValidation();
            }
            menu.start();
        }
    }
}
