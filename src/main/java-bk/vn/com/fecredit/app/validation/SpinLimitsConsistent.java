package vn.com.fecredit.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a participant's daily spin limit does not exceed their total spin limit.
 * This annotation should be applied at the class level on entities that manage spin limits.
 * 
 * Example usage:
 * {@code @SpinLimitsConsistent}
 * public class Participant { ... }
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SpinLimitsConsistentValidator.class)
@Documented
public @interface SpinLimitsConsistent {
    
    /**
     * @return the error message template
     */
    String message() default "{validation.constraints.spinlimits.inconsistent}";

    /**
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Defines several {@link SpinLimitsConsistent} annotations on the same element.
     */
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        SpinLimitsConsistent[] value();
    }
}
