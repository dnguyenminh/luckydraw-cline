package vn.com.fecredit.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.fecredit.app.dto.common.BaseRequest;
import vn.com.fecredit.app.util.ValidationUtils;
import vn.com.fecredit.app.exception.BusinessException;

@Data
@EqualsAndHashCode(callSuper = true)
public class ForgotPasswordRequest extends BaseRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Type is required")
    @Size(max = 20, message = "Type must not exceed 20 characters")
    private String type; // EMAIL or SMS

    // Optional phone number for SMS type
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;

    @Override
    protected void validateFields() {
        // Basic validation
        getRequired(email, "email");
        getRequired(type, "type");

        // Format validation
        if (!ValidationUtils.isValidEmail(email)) {
            throw new BusinessException("Invalid email format");
        }

        // Type validation
        type = type.toUpperCase();
        if (!type.equals("EMAIL") && !type.equals("SMS")) {
            throw new BusinessException("Type must be either EMAIL or SMS");
        }

        // Phone number validation for SMS type
        if (type.equals("SMS")) {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new BusinessException("Phone number is required for SMS type");
            }
            if (!ValidationUtils.isValidPhone(phoneNumber)) {
                throw new BusinessException("Invalid phone number format");
            }
        }

        // Trim inputs
        email = getTrimmedOrNull(email);
        phoneNumber = getTrimmedOrNull(phoneNumber);
    }
}
