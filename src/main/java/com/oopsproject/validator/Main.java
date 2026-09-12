package com.oopsproject.validator;

import com.oopsproject.validator.ui.ConsoleMenu;
import com.oopsproject.validator.ui.ValidatorGUI;

import javax.swing.SwingUtilities;

/**
 * Entry point for the AI Dataset Coordinate Validator.
 */
public class Main {

    public static void main(String[] args) {
        boolean launchGui = false;
        String autoLoadPath = null;

        for (String arg : args) {
            if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                launchGui = true;
            } else if (autoLoadPath == null && !arg.startsWith("-")) {
                autoLoadPath = arg;
            }
        }

        if (launchGui) {
            System.out.println("Starting AI Dataset Coordinate Validator in GUI mode...");
            SwingUtilities.invokeLater(() -> {
                ValidatorGUI gui = new ValidatorGUI();
                gui.setVisible(true);
            });
        } else {
            ConsoleMenu menu = new ConsoleMenu();
            if (autoLoadPath != null) {
                System.out.println("Auto-loading dataset: " + autoLoadPath);
                menu.loadDataset(autoLoadPath);
                menu.runValidation();
            }
            menu.start();
        }
    }
}
