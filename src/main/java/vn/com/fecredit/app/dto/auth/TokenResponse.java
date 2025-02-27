package vn.com.fecredit.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.fecredit.app.dto.common.BaseResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse extends BaseResponse {

    private final String token;
    private final String refreshToken;
    private final String tokenType;
    private final Long expiresIn;

    private TokenResponse(String token, String refreshToken, Long expiresIn) {
        super(true, "Token generated successfully", null);
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    private TokenResponse(String message, String errorCode) {
        super(false, message, errorCode);
        this.token = null;
        this.refreshToken = null;
        this.tokenType = null;
        this.expiresIn = null;
    }

    public static TokenResponse of(String token, String refreshToken, Long expiresIn) {
        return new TokenResponse(token, refreshToken, expiresIn);
    }

    public static TokenResponse ofAccessToken(String token, Long expiresIn) {
        return new TokenResponse(token, null, expiresIn);
    }

    public static TokenResponse ofRefreshToken(String refreshToken) {
        return new TokenResponse(null, refreshToken, null);
    }

    public static TokenResponse error(String message) {
        return new TokenResponse(message, "TOKEN_ERROR");
    }

    public static TokenResponse expired() {
        return new TokenResponse("Token has expired", "TOKEN_EXPIRED");
    }

    public static TokenResponse invalid() {
        return new TokenResponse("Invalid token", "TOKEN_INVALID");
    }

    public static TokenResponse refreshRequired() {
        return new TokenResponse("Token refresh required", "TOKEN_REFRESH_REQUIRED");
    }

    public boolean hasValidAccessToken() {
        return token != null && !token.isEmpty();
    }

    public boolean hasValidRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("TokenResponse(success=%s, message=%s, errorCode=%s, tokenType=%s, expiresIn=%d)",
            isSuccess(), getMessage(), getErrorCode(), tokenType, expiresIn);
    }
}
