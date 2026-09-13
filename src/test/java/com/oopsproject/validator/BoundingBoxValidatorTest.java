package com.oopsproject.validator;

import com.oopsproject.validator.model.Coordinate;
import com.oopsproject.validator.service.BoundingBoxValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoundingBoxValidatorTest {

    @Test
    @DisplayName("Test BoundingBoxValidator regional boundary conditions")
    public void testBoundingBoxValidator() {
        // Default India region: lat [6.0, 37.5], lon [68.0, 97.5]
        BoundingBoxValidator validator = new BoundingBoxValidator();

        Coordinate delhi = new Coordinate(1, "Delhi", 28.6139, 77.2090);
        Coordinate london = new Coordinate(2, "London", 51.5074, -0.1278);

        assertTrue(validator.isValid(delhi, null));
        assertFalse(validator.isValid(london, null));

        assertTrue(validator.getErrorMessage(london, null).contains("outside India Region bounds"));
    }
}
