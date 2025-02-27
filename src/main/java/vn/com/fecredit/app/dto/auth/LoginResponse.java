package vn.com.fecredit.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.fecredit.app.dto.common.BaseResponse;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse extends BaseResponse {

    private final String token;
    private final String refreshToken;
    private final String tokenType;
    private final Long expiresIn;
    private final String username;
    private final Set<String> roles;
    private final UserInfo userInfo;

    public LoginResponse(
            String token,
            String refreshToken,
            Long expiresIn,
            String username,
            Set<String> roles,
            UserInfo userInfo
    ) {
        super(true, "Login successful", null);
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.username = username;
        this.roles = roles;
        this.userInfo = userInfo;
    }

    public static LoginResponse of(
            String token,
            String refreshToken,
            Long expiresIn,
            String username,
            Set<String> roles,
            UserInfo userInfo
    ) {
        return new LoginResponse(token, refreshToken, expiresIn, username, roles, userInfo);
    }

    public static LoginResponse error(String message) {
        return new LoginResponse(null, null, null, null, null, null) {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    @Data
    public static class UserInfo {
        private final Long id;
        private final String fullName;
        private final String email;
        private final String phoneNumber;
        private final String avatarUrl;
        private final Boolean isActive;

        public static UserInfo of(
                Long id,
                String fullName,
                String email,
                String phoneNumber,
                String avatarUrl,
                Boolean isActive
        ) {
            return new UserInfo(id, fullName, email, phoneNumber, avatarUrl, isActive);
        }
    }
}
