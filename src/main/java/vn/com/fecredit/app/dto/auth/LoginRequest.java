package vn.com.fecredit.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.fecredit.app.dto.common.BaseRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginRequest extends BaseRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Override
    protected void validateFields() {
        getRequired(username, "username");
        getRequired(password, "password");
    }
}
