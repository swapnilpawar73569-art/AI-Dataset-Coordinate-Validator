package com.oopsproject.validator.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.model.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class JsonReportExporter implements ReportExporter {
    @Override
    public String getSupportedExtension() {
        return ".json";
    }

    @Override
    public void export(ValidationReport report, File targetFile) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("totalRows", report.getTotalCount());
        root.addProperty("validRows", report.getValidCount());
        root.addProperty("invalidRows", report.getInvalidCount());

        JsonArray resultsArray = new JsonArray();
        for (ValidationResult res : report.getResults()) {
            JsonObject item = new JsonObject();
            var c = res.getCoordinate();
            item.addProperty("rowNumber", c.getRowNumber());
            item.addProperty("label", c.getLabel());
            item.addProperty("rawLatitude", c.getRawLatitude());
            item.addProperty("rawLongitude", c.getRawLongitude());
            item.addProperty("status", res.isValid() ? "VALID" : "INVALID");
            
            JsonArray errors = new JsonArray();
            for (String err : res.getErrorMessages()) {
                errors.add(err);
            }
            item.add("failureReasons", errors);
            resultsArray.add(item);
        }
        root.add("results", resultsArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.println(gson.toJson(root));
        }
    }
}
