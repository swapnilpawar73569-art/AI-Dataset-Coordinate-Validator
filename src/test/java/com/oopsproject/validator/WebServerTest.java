package com.oopsproject.validator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oopsproject.validator.server.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class WebServerTest {

    private static WebServer webServer;
    private static int testPort = 18080;
    private static HttpClient client;

    @BeforeAll
    public static void setUp() throws IOException {
        webServer = new WebServer(testPort);
        webServer.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    public static void tearDown() {
        if (webServer != null) {
            webServer.stop();
        }
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/api/health"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\": \"UP\"") || response.body().contains("\"status\":\"UP\""));
    }

    @Test
    public void testSampleCsvEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/api/sample?format=csv"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("latitude,longitude"));
        assertTrue(response.body().contains("New_Delhi"));
    }

    @Test
    public void testValidateCsvApi() throws Exception {
        String csvData = "label,latitude,longitude\\nDelhi,28.6139,77.2090\\nBadLat,95.0,40.0";
        String payload = "{\"format\":\"csv\",\"data\":\"" + csvData + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/api/validate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        assertTrue(json.get("success").getAsBoolean());
        JsonObject summary = json.getAsJsonObject("summary");
        assertEquals(2, summary.get("totalCount").getAsInt());
        assertEquals(1, summary.get("validCount").getAsInt());
        assertEquals(1, summary.get("invalidCount").getAsInt());
    }

    @Test
    public void testExportApi() throws Exception {
        String csvData = "label,latitude,longitude\\nDelhi,28.6139,77.2090";
        String payload = "{\"format\":\"csv\",\"exportType\":\"csv\",\"data\":\"" + csvData + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/api/export"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Delhi"));
    }

    @Test
    public void testStaticIndexHtmlServing() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/html"));
        assertTrue(response.body().contains("GeoValidate"));
    }

    @Test
    public void testStaticCssServing() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/style.css"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/css"));
        assertTrue(response.body().contains("--bg-main"));
    }
}
