package com.oopsproject.validator.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates full validation report statistics for a dataset.
 */
public class ValidationReport {
    private final List<ValidationResult> results;
    private final int totalCount;
    private final int validCount;
    private final int invalidCount;

    public ValidationReport(List<ValidationResult> results) {
        this.results = results != null ? results : Collections.emptyList();
        this.totalCount = this.results.size();
        int valid = 0;
        for (ValidationResult res : this.results) {
            if (res.isValid()) {
                valid++;
            }
        }
        this.validCount = valid;
        this.invalidCount = this.totalCount - this.validCount;
    }

    public List<ValidationResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public List<ValidationResult> getValidResults() {
        return results.stream().filter(ValidationResult::isValid).collect(Collectors.toList());
    }

    public List<ValidationResult> getInvalidResults() {
        return results.stream().filter(r -> !r.isValid()).collect(Collectors.toList());
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getValidCount() {
        return validCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== VALIDATION REPORT =====\n");
        for (ValidationResult res : results) {
            sb.append(res.toString()).append("\n");
        }
        sb.append("-----------------------------\n");
        sb.append(String.format("Summary: Total Rows: %d | Valid: %d | Invalid: %d\n",
                totalCount, validCount, invalidCount));
        sb.append("=============================");
        return sb.toString();
    }
}
