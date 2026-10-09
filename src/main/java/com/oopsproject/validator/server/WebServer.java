package com.oopsproject.validator.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;
import com.oopsproject.validator.service.BoundingBoxValidator;
import com.oopsproject.validator.service.DatasetLoader;
import com.oopsproject.validator.service.DatasetLoaderFactory;
import com.oopsproject.validator.service.DuplicateValidator;
import com.oopsproject.validator.service.FormatValidator;
import com.oopsproject.validator.service.OutlierValidator;
import com.oopsproject.validator.service.PrecisionValidator;
import com.oopsproject.validator.service.RangeValidator;
import com.oopsproject.validator.service.ValidationEngine;
import com.oopsproject.validator.util.CsvReportExporter;
import com.oopsproject.validator.util.HtmlReportExporter;
import com.oopsproject.validator.util.JsonReportExporter;
import com.oopsproject.validator.util.ReportExporter;
import com.oopsproject.validator.util.TxtReportExporter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Built-in embedded HTTP server providing REST API endpoints and serving the modern Web Dashboard.
 * Powered by standard JDK HttpServer with zero extra external dependencies.
 */
public class WebServer {

    private final int port;
    private HttpServer server;
    private final Gson gson;

    public WebServer(int port) {
        this.port = port;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public synchronized void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        // REST API routes
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/validate", new ValidateHandler());
        server.createContext("/api/sample", new SampleHandler());
        server.createContext("/api/export", new ExportHandler());

        // Static files (Web frontend)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("==================================================================");
        System.out.println("   📍 AI DATASET COORDINATE VALIDATOR — WEB DASHBOARD");
        System.out.println("==================================================================");
        System.out.println("   ➜ Dashboard URL: http://localhost:" + port + "/");
        System.out.println("   ➜ Health API:    http://localhost:" + port + "/api/health");
        System.out.println("==================================================================");
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public int getPort() {
        return port;
    }

    /**
     * Opens default web browser to the dashboard URL.
     */
    public void openBrowser() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + port + "/"));
            }
        } catch (Exception e) {
            System.out.println("Browser auto-open not supported in this environment: " + e.getMessage());
        }
    }

    // ==================== HTTP HANDLERS ====================

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            JsonObject res = new JsonObject();
            res.addProperty("status", "UP");
            res.addProperty("application", "AI Dataset Coordinate Validator");
            res.addProperty("version", "1.0.0");
            res.addProperty("jvmVersion", System.getProperty("java.version"));

            JsonArray validators = new JsonArray();
            validators.add("RangeValidator");
            validators.add("FormatValidator");
            validators.add("DuplicateValidator");
            validators.add("PrecisionValidator");
            validators.add("OutlierValidator");
            validators.add("BoundingBoxValidator");
            res.add("supportedValidators", validators);

            sendJsonResponse(exchange, 200, res.toString());
        }
    }

    private class SampleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            String query = exchange.getRequestURI().getQuery();
            boolean isJson = query != null && query.toLowerCase().contains("format=json");

            if (isJson) {
                String sampleJson = getSampleJsonContent();
                sendResponse(exchange, 200, "application/json; charset=UTF-8", sampleJson.getBytes(StandardCharsets.UTF_8));
            } else {
                String sampleCsv = getSampleCsvContent();
                sendResponse(exchange, 200, "text/csv; charset=UTF-8", sampleCsv.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private class ValidateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (requestBody.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Empty request body.\"}");
                    return;
                }

                JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
                String rawData = json.has("data") ? json.get("data").getAsString() : "";
                String format = json.has("format") ? json.get("format").getAsString() : "csv";

                if (rawData.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Field 'data' cannot be empty.\"}");
                    return;
                }

                // Parse coordinates using Factory Pattern
                DatasetLoader loader = DatasetLoaderFactory.getLoaderForFormat(format);
                List<Coordinate> coordinates = loader.loadFromString(rawData);

                // Build configured Validation Engine (Strategy Pattern)
                JsonObject opts = json.has("options") && json.get("options").isJsonObject()
                        ? json.getAsJsonObject("options") : new JsonObject();

                ValidationEngine engine = createConfiguredEngine(opts);
                ValidationReport report = engine.validate(coordinates);

                // Build rich response payload
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);

                // Summary KPIs
                JsonObject summary = new JsonObject();
                summary.addProperty("totalCount", report.getTotalCount());
                summary.addProperty("validCount", report.getValidCount());
                summary.addProperty("invalidCount", report.getInvalidCount());
                summary.addProperty("passRate", Math.round(report.getPassRate() * 100.0) / 100.0);
                summary.addProperty("duplicateCount", report.getDuplicateCount());
                summary.addProperty("outlierCount", report.getOutlierCount());
                summary.addProperty("bboxBreachCount", report.getBoundingBoxBreachCount());
                summary.addProperty("rangeErrorCount", report.getRangeErrorCount());
                summary.addProperty("formatErrorCount", report.getFormatErrorCount());
                responseJson.add("summary", summary);

                // Per-row item details
                JsonArray resultsArray = new JsonArray();
                for (ValidationResult res : report.getResults()) {
                    Coordinate c = res.getCoordinate();
                    JsonObject item = new JsonObject();
                    item.addProperty("rowNumber", c.getRowNumber());
                    item.addProperty("label", c.getLabel());
                    item.add("latitude", c.getLatitude() != null ? gson.toJsonTree(c.getLatitude()) : null);
                    item.add("longitude", c.getLongitude() != null ? gson.toJsonTree(c.getLongitude()) : null);
                    item.addProperty("rawLatitude", c.getRawLatitude());
                    item.addProperty("rawLongitude", c.getRawLongitude());
                    item.addProperty("isValid", res.isValid());
                    item.addProperty("status", res.isValid() ? "VALID" : "INVALID");

                    JsonArray errors = new JsonArray();
                    for (String err : res.getErrorMessages()) {
                        errors.add(err);
                    }
                    item.add("errors", errors);
                    item.addProperty("formattedErrors", res.getFormattedErrors());
                    item.addProperty("category", categorizeResult(res));

                    resultsArray.add(item);
                }
                responseJson.add("results", resultsArray);

                sendJsonResponse(exchange, 200, gson.toJson(responseJson));

            } catch (Exception e) {
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("error", e.getMessage());
                sendJsonResponse(exchange, 400, gson.toJson(err));
            }
        }
    }

    private class ExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
                String rawData = json.has("data") ? json.get("data").getAsString() : "";
                String format = json.has("format") ? json.get("format").getAsString() : "csv";
                String exportType = json.has("exportType") ? json.get("exportType").getAsString().toLowerCase() : "html";

                DatasetLoader loader = DatasetLoaderFactory.getLoaderForFormat(format);
                List<Coordinate> coordinates = loader.loadFromString(rawData);

                JsonObject opts = json.has("options") && json.get("options").isJsonObject()
                        ? json.getAsJsonObject("options") : new JsonObject();
                ValidationEngine engine = createConfiguredEngine(opts);
                ValidationReport report = engine.validate(coordinates);

                ReportExporter exporter;
                String mimeType;
                String extension;

                switch (exportType) {
                    case "csv":
                        exporter = new CsvReportExporter();
                        mimeType = "text/csv; charset=UTF-8";
                        extension = ".csv";
                        break;
                    case "json":
                        exporter = new JsonReportExporter();
                        mimeType = "application/json; charset=UTF-8";
                        extension = ".json";
                        break;
                    case "txt":
                        exporter = new TxtReportExporter();
                        mimeType = "text/plain; charset=UTF-8";
                        extension = ".txt";
                        break;
                    case "html":
                    default:
                        exporter = new HtmlReportExporter();
                        mimeType = "text/html; charset=UTF-8";
                        extension = ".html";
                        break;
                }

                File tempFile = File.createTempFile("validation_report_", extension);
                try {
                    exporter.export(report, tempFile);
                    byte[] bytes;
                    try (FileInputStream fis = new FileInputStream(tempFile)) {
                        bytes = fis.readAllBytes();
                    }
                    exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"validation_report" + extension + "\"");
                    sendResponse(exchange, 200, mimeType, bytes);
                } finally {
                    tempFile.delete();
                }

            } catch (Exception e) {
                JsonObject err = new JsonObject();
                err.addProperty("error", "Export failed: " + e.getMessage());
                sendJsonResponse(exchange, 400, gson.toJson(err));
            }
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.trim().isEmpty()) {
                path = "/index.html";
            }

            byte[] content = loadResourceBytes("/web" + path);
            if (content == null) {
                // Fallback for SPA routing to index.html if file has no extension
                if (!path.contains(".")) {
                    content = loadResourceBytes("/web/index.html");
                }
            }

            if (content == null) {
                String notFound = "<h1>404 Not Found</h1><p>Resource " + path + " was not found on this server.</p>";
                sendResponse(exchange, 404, "text/html; charset=UTF-8", notFound.getBytes(StandardCharsets.UTF_8));
                return;
            }

            String mimeType = getMimeType(path);
            sendResponse(exchange, 200, mimeType, content);
        }
    }

    // ==================== HELPER METHODS ====================

    private ValidationEngine createConfiguredEngine(JsonObject opts) {
        ValidationEngine engine = new ValidationEngine();

        boolean checkFormat = !opts.has("checkFormat") || opts.get("checkFormat").getAsBoolean();
        boolean checkRange = !opts.has("checkRange") || opts.get("checkRange").getAsBoolean();
        boolean checkDuplicates = !opts.has("checkDuplicates") || opts.get("checkDuplicates").getAsBoolean();
        boolean checkOutliers = !opts.has("checkOutliers") || opts.get("checkOutliers").getAsBoolean();
        boolean checkPrecision = !opts.has("checkPrecision") || opts.get("checkPrecision").getAsBoolean();
        boolean checkBoundingBox = !opts.has("checkBoundingBox") || opts.get("checkBoundingBox").getAsBoolean();

        if (checkFormat) {
            engine.addValidator(new FormatValidator());
        }
        if (checkRange) {
            engine.addValidator(new RangeValidator());
        }
        if (checkDuplicates) {
            engine.addValidator(new DuplicateValidator());
        }
        if (checkOutliers) {
            double multiplier = opts.has("outlierIqr") ? opts.get("outlierIqr").getAsDouble() : 1.5;
            engine.addValidator(new OutlierValidator(multiplier));
        }
        if (checkPrecision) {
            int precision = opts.has("precisionPlaces") ? opts.get("precisionPlaces").getAsInt() : 6;
            engine.addValidator(new PrecisionValidator(precision));
        }
        if (checkBoundingBox) {
            String region = opts.has("bboxRegion") ? opts.get("bboxRegion").getAsString() : "India Region";
            double minLat = opts.has("bboxMinLat") ? opts.get("bboxMinLat").getAsDouble() : 6.0;
            double maxLat = opts.has("bboxMaxLat") ? opts.get("bboxMaxLat").getAsDouble() : 37.5;
            double minLon = opts.has("bboxMinLon") ? opts.get("bboxMinLon").getAsDouble() : 68.0;
            double maxLon = opts.has("bboxMaxLon") ? opts.get("bboxMaxLon").getAsDouble() : 97.5;
            engine.addValidator(new BoundingBoxValidator(region, minLat, maxLat, minLon, maxLon));
        }

        return engine;
    }

    private String categorizeResult(ValidationResult res) {
        if (res.isValid()) {
            return "valid";
        }
        String errors = res.getFormattedErrors().toLowerCase();
        if (errors.contains("missing") || errors.contains("format") || errors.contains("nan")) {
            return "format";
        }
        if (errors.contains("range [-")) {
            return "range";
        }
        if (errors.contains("duplicate")) {
            return "duplicate";
        }
        if (errors.contains("outlier")) {
            return "outlier";
        }
        if (errors.contains("outside") || errors.contains("bounds")) {
            return "bbox";
        }
        return "invalid";
    }

    private boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, status, "application/json; charset=UTF-8", bytes);
    }

    private void sendResponse(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private byte[] loadResourceBytes(String resourcePath) {
        // 1. Try class loader (for packaged JAR or compiled classpath)
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception ignored) {
        }

        // 2. Try file system relative locations
        String cleanPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        File[] searchLocations = new File[]{
                new File("src/main/resources/" + cleanPath),
                new File(cleanPath),
                new File("web/" + cleanPath.replace("web/", ""))
        };

        for (File f : searchLocations) {
            if (f.exists() && f.isFile()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    return fis.readAllBytes();
                } catch (IOException ignored) {
                }
            }
        }

        return null;
    }

    private String getMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }

    private String getSampleCsvContent() {
        return "label,latitude,longitude\n" +
                "New_Delhi,28.613900,77.209000\n" +
                "Mumbai,19.076000,72.877700\n" +
                "Bengaluru,12.971600,77.594600\n" +
                "Hyderabad,17.385000,78.486700\n" +
                "Chennai,13.082700,80.270700\n" +
                "Kolkata,22.572600,88.363900\n" +
                "Ahmedabad,23.022500,72.571400\n" +
                "Jaipur,26.912400,75.787300\n" +
                "Delhi_Duplicate,28.613900,77.209000\n" +
                "Error_LatOver90,98.543200,75.200000\n" +
                "Error_LonOver180,24.120000,195.430000\n" +
                "Error_MissingLat,,77.102500\n" +
                "Error_CorruptFormat,21.170240,NOT_A_NUMBER\n" +
                "Outlier_Antarctica,-82.862800,135.000000\n" +
                "Outlier_Greenland,72.000000,-40.000000\n";
    }

    private String getSampleJsonContent() {
        return "[\n" +
                "  {\"label\": \"New_Delhi\", \"latitude\": 28.6139, \"longitude\": 77.2090},\n" +
                "  {\"label\": \"Mumbai\", \"latitude\": 19.0760, \"longitude\": 72.8777},\n" +
                "  {\"label\": \"Bengaluru\", \"latitude\": 12.9716, \"longitude\": 77.5946},\n" +
                "  {\"label\": \"Hyderabad\", \"latitude\": 17.3850, \"longitude\": 78.4867},\n" +
                "  {\"label\": \"Chennai\", \"latitude\": 13.0827, \"longitude\": 80.2707},\n" +
                "  {\"label\": \"Delhi_Duplicate\", \"latitude\": 28.6139, \"longitude\": 77.2090},\n" +
                "  {\"label\": \"Error_LatOver90\", \"latitude\": 98.5432, \"longitude\": 75.2000},\n" +
                "  {\"label\": \"Error_MissingLat\", \"latitude\": null, \"longitude\": 77.1025},\n" +
                "  {\"label\": \"Error_CorruptFormat\", \"latitude\": 21.17024, \"longitude\": \"BAD_VALUE\"},\n" +
                "  {\"label\": \"Outlier_Antarctica\", \"latitude\": -82.8628, \"longitude\": 135.0000}\n" +
                "]";
    }
}
