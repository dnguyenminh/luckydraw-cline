package vn.com.fecredit.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.com.fecredit.app.entity.Participant;

public class SpinLimitsConsistentValidator implements ConstraintValidator<SpinLimitsConsistent, Participant> {

    @Override
    public void initialize(SpinLimitsConsistent constraintAnnotation) {
    }

    @Override
    public boolean isValid(Participant participant, ConstraintValidatorContext context) {
        if (participant == null || participant.getDailySpinLimit() == null || participant.getTotalSpinLimit() == null) {
            return true; // Let @NotNull handle null values
        }

        if (participant.getDailySpinLimit() > participant.getTotalSpinLimit()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Daily spin limit (" + participant.getDailySpinLimit() + 
                    ") cannot exceed total spin limit (" + participant.getTotalSpinLimit() + ")")
                .addConstraintViolation();
            return false;
        }

        return true;
    }
}
