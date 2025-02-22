package vn.com.fecredit.app.validation;

import java.time.LocalDateTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.com.fecredit.app.dto.GoldenHourDTO;

public class ValidTimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {
    
    @Override
    public void initialize(ValidTimeRange constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null objects
        }

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        if (value instanceof GoldenHourDTO.CreateRequest) {
            GoldenHourDTO.CreateRequest request = (GoldenHourDTO.CreateRequest) value;
            startTime = request.getStartTime();
            endTime = request.getEndTime();
            
            // For create requests, both times must be present
            if (startTime == null || endTime == null) {
                return false;
            }
        } else if (value instanceof GoldenHourDTO.UpdateRequest) {
            GoldenHourDTO.UpdateRequest request = (GoldenHourDTO.UpdateRequest) value;
            startTime = request.getStartTime();
            endTime = request.getEndTime();
            
            // For update requests, if either time is not set, consider it valid
            if (startTime == null || endTime == null) {
                return true;
            }
        }

        // When both times are present, validate end time is after start time
        return endTime.isAfter(startTime);
    }
}