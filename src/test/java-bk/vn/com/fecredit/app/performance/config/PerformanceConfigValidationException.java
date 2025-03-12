package vn.com.fecredit.app.performance.config;

import java.util.Collections;
import java.util.List;

public class PerformanceConfigValidationException extends RuntimeException {
    private final List<String> validationErrors;

    public PerformanceConfigValidationException(String message) {
        super(message);
        this.validationErrors = Collections.singletonList(message);
    }

    public PerformanceConfigValidationException(List<String> errors) {
        super("Performance configuration validation failed");
        this.validationErrors = errors;
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    @Override
    public String getMessage() {
        return String.format("Performance configuration validation failed:%n - %s", 
            String.join("\n - ", validationErrors));
    }

    public static PerformanceConfigValidationException fromErrors(List<String> errors) {
        return new PerformanceConfigValidationException(errors);
    }

    public static PerformanceConfigValidationException fromError(String error) {
        return new PerformanceConfigValidationException(error);
    }
}
