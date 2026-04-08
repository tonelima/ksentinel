package com.konstroi.ksentinel.infrastructure.validation;

import java.util.List;

public record ValidationResult(boolean passed, String details) {

    public static ValidationResult ok() {
        return new ValidationResult(true, "All validations passed");
    }

    public static ValidationResult fail(String reason) {
        return new ValidationResult(false, reason);
    }

    public static ValidationResult aggregate(List<ValidationResult> results) {
        List<String> failures = results.stream()
                .filter(r -> !r.passed())
                .map(ValidationResult::details)
                .toList();

        if (failures.isEmpty()) {
            return ok();
        }
        return fail(String.join("; ", failures));
    }
}
