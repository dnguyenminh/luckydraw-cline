package vn.com.fecredit.app.util;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import vn.com.fecredit.app.dto.AuthResponse;

/**
 * Authentication utilities for tests.
 * Provides methods to handle authentication in test scenarios.
 */
public final class Auth {

    private Auth() {
        // Prevent instantiation
    }

    /**
     * Performs login and returns the authentication token.
     */
    public static String getAuthToken(MockMvc mockMvc) throws Exception {
        ResultActions result = login(mockMvc);
        return extractToken(result);
    }

    /**
     * Performs login and returns the full auth response.
     */
    public static AuthResponse getAuthResponse(MockMvc mockMvc) throws Exception {
        ResultActions result = login(mockMvc);
        return TestResponseUtils.extractAuthResponse(result);
    }

    /**
     * Performs login with default test credentials.
     */
    public static ResultActions login(MockMvc mockMvc) throws Exception {
        return login(mockMvc, TestData.TEST_EMAIL, TestData.TEST_PASSWORD);
    }

    /**
     * Performs login with specified credentials.
     */
    public static ResultActions login(MockMvc mockMvc, String email, String password) throws Exception {
        return TestRequestUtils.performLogin(mockMvc, email, password);
    }

    /**
     * Creates an authorization header value with the token.
     */
    public static String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * Extracts the token from a login result.
     */
    private static String extractToken(ResultActions result) throws Exception {
        return TestResponseUtils.extractToken(result);
    }
}