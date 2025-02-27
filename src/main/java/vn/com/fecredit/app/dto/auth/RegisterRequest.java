package vn.com.fecredit.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.fecredit.app.dto.common.BaseRequest;
import vn.com.fecredit.app.util.ValidationUtils;
import vn.com.fecredit.app.exception.BusinessException;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterRequest extends BaseRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, one special character and no whitespace"
    )
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String avatarUrl;

    @Override
    protected void validateFields() {
        // Basic validations
        getRequired(username, "username");
        getRequired(password, "password");
        getRequired(fullName, "fullName");
        getRequired(email, "email");
        getRequired(phoneNumber, "phoneNumber");

        // Format validations
        if (!ValidationUtils.isValidUsername(username)) {
            throw new BusinessException("Invalid username format");
        }

        if (!ValidationUtils.isValidPassword(password)) {
            throw new BusinessException("Invalid password format");
        }

        if (!ValidationUtils.isValidEmail(email)) {
            throw new BusinessException("Invalid email format");
        }

        if (!ValidationUtils.isValidPhone(phoneNumber)) {
            throw new BusinessException("Invalid phone number format");
        }

        // Trim strings
        username = getTrimmedOrNull(username);
        fullName = getTrimmedOrNull(fullName);
        email = getTrimmedOrNull(email);
        phoneNumber = getTrimmedOrNull(phoneNumber);
        avatarUrl = getTrimmedOrNull(avatarUrl);
    }
}
