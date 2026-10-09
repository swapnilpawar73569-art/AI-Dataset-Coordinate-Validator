package com.oopsproject.validator;

import com.oopsproject.validator.server.WebServer;
import com.oopsproject.validator.ui.ConsoleMenu;
import com.oopsproject.validator.ui.ValidatorGUI;

import javax.swing.SwingUtilities;

/**
 * Entry point for the AI Dataset Coordinate Validator.
 * Supports Web Dashboard (--web / default), Desktop Swing GUI (--gui), and Terminal CLI (--cli).
 */
public class Main {

    public static void main(String[] args) {
        boolean launchGui = false;
        boolean launchCli = false;
        boolean launchWeb = false;
        int port = 8080;
        String autoLoadPath = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                launchGui = true;
            } else if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                launchCli = true;
            } else if ("--web".equalsIgnoreCase(arg) || "-w".equalsIgnoreCase(arg)) {
                launchWeb = true;
            } else if (("--port".equalsIgnoreCase(arg) || "-p".equalsIgnoreCase(arg)) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {
                }
            } else if (autoLoadPath == null && !arg.startsWith("-")) {
                autoLoadPath = arg;
            }
        }

        // If no explicit mode chosen, default to the Web Dashboard
        if (!launchGui && !launchCli && !launchWeb) {
            launchWeb = true;
        }

        if (launchGui) {
            System.out.println("Starting AI Dataset Coordinate Validator in Desktop GUI mode...");
            SwingUtilities.invokeLater(() -> {
                ValidatorGUI gui = new ValidatorGUI();
                gui.setVisible(true);
            });
        } else if (launchWeb) {
            try {
                WebServer webServer = new WebServer(port);
                webServer.start();
                webServer.openBrowser();

                System.out.println("Press Ctrl+C in terminal to stop web server.");
                System.out.println("(Tip: Run with '--gui' for Desktop Swing UI, or '--cli' for Terminal Menu)\n");

                // Keep process alive while server runs
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\nShutting down Coordinate Validator Web Server...");
                    webServer.stop();
                }));

                // Block main thread to keep web server running indefinitely
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                System.out.println("Web server interrupted.");
            } catch (Exception e) {
                System.err.println("Failed to start Web Server: " + e.getMessage());
                System.out.println("Falling back to Console Menu...");
                runConsoleMenu(autoLoadPath);
            }
        } else {
            runConsoleMenu(autoLoadPath);
        }
    }

    private static void runConsoleMenu(String autoLoadPath) {
        ConsoleMenu menu = new ConsoleMenu();
        if (autoLoadPath != null) {
            System.out.println("Auto-loading dataset: " + autoLoadPath);
            menu.loadDataset(autoLoadPath);
            menu.runValidation();
        }
        menu.start();
    }
}
