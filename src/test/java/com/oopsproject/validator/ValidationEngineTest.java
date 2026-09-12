package com.oopsproject.validator;

import com.oopsproject.validator.exception.ValidationException;
import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.model.ValidationReport;
import com.oopsproject.validator.service.ValidationEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationEngineTest {

    @Test
    @DisplayName("Test ValidationEngine with valid and invalid coordinates")
    public void testValidationEnginePipeline() throws ValidationException {
        ValidationEngine engine = ValidationEngine.createDefaultEngine();

        Coordinate valid1 = new Coordinate(1, "Valid1", 28.6139, 77.2090);
        Coordinate valid2 = new Coordinate(2, "Valid2", 19.0760, 72.8777);
        Coordinate invalidRange = new Coordinate(3, "InvalidRange", 95.0, 45.0);
        Coordinate missingVal = new Coordinate(4, "MissingVal", null, 12.3456);

        List<Coordinate> dataset = Arrays.asList(valid1, valid2, invalidRange, missingVal);
        ValidationReport report = engine.validate(dataset);

        assertNotNull(report);
        assertEquals(4, report.getTotalCount());
        assertEquals(2, report.getValidCount());
        assertEquals(2, report.getInvalidCount());
    }
}
