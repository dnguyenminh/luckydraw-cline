package vn.com.fecredit.app.util.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.api.ObjectAssert;
import vn.com.fecredit.app.dto.AuthResponse;
import vn.com.fecredit.app.dto.UserDto;

/**
 * Custom assertions for AuthResponse validation.
 */
public class AuthAssertions extends AbstractAssert<AuthAssertions, AuthResponse> {

    private AuthAssertions(AuthResponse actual) {
        super(actual, AuthAssertions.class);
    }

    public static AuthAssertions assertThat(AuthResponse actual) {
        return new AuthAssertions(actual);
    }

    /**
     * Verifies the response has a valid token.
     */
    public AuthAssertions hasValidToken() {
        isNotNull();
        new StringAssert(actual.getToken())
            .isNotNull()
            .isNotBlank();
        return this;
    }

    /**
     * Verifies the token type matches expected value.
     */
    public AuthAssertions hasTokenType(String expected) {
        isNotNull();
        new StringAssert(actual.getTokenType())
            .isEqualTo(expected);
        return this;
    }

    /**
     * Verifies the response contains valid user information.
     */
    public AuthAssertions hasValidUser() {
        isNotNull();
        UserDto user = actual.getUser();
        new ObjectAssert<>(user).isNotNull();
        new StringAssert(user.getEmail()).isNotBlank();
        return this;
    }
}