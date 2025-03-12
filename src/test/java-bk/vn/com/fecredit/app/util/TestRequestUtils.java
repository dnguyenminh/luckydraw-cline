package vn.com.fecredit.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import vn.com.fecredit.app.dto.AuthRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static vn.com.fecredit.app.util.TestConstants.Api.*;
import static vn.com.fecredit.app.util.TestConstants.Auth.BEARER_PREFIX;

/**
 * Utility methods for building and performing test HTTP requests.
 */
public final class TestRequestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TestRequestUtils() {
        // Prevent instantiation
    }

    /**
     * Performs a login request and expects a successful response.
     */
    public static ResultActions performLogin(MockMvc mockMvc, String email, String password) throws Exception {
        AuthRequest request = AuthRequest.builder()
            .email(email)
            .password(password)
            .build();

        return mockMvc.perform(createLoginRequest(request))
            .andExpect(status().isOk());
    }

    /**
     * Performs a GET request with authentication token.
     */
    public static ResultActions performGetWithToken(
            MockMvc mockMvc, 
            String endpoint, 
            String token) throws Exception {
        return mockMvc.perform(createAuthenticatedGet(endpoint, token));
    }

    /**
     * Performs a POST request with authentication token.
     */
    public static ResultActions performPostWithToken(
            MockMvc mockMvc, 
            String endpoint, 
            Object body, 
            String token) throws Exception {
        return mockMvc.perform(createAuthenticatedPost(endpoint, body, token));
    }

    /**
     * Creates a login request.
     */
    public static RequestBuilder createLoginRequest(Object requestBody) throws Exception {
        return MockMvcRequestBuilders.post(LOGIN_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody));
    }

    /**
     * Creates an authenticated GET request.
     */
    public static MockHttpServletRequestBuilder createAuthenticatedGet(
            String endpoint, 
            String token) {
        return MockMvcRequestBuilders.get(endpoint)
            .header(AUTHORIZATION, BEARER_PREFIX + token);
    }

    /**
     * Creates an authenticated POST request.
     */
    public static MockHttpServletRequestBuilder createAuthenticatedPost(
            String endpoint, 
            Object body, 
            String token) throws Exception {
        return MockMvcRequestBuilders.post(endpoint)
            .header(AUTHORIZATION, BEARER_PREFIX + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body));
    }

    /**
     * Creates an authenticated PUT request.
     */
    public static MockHttpServletRequestBuilder createAuthenticatedPut(
            String endpoint, 
            Object body, 
            String token) throws Exception {
        return MockMvcRequestBuilders.put(endpoint)
            .header(AUTHORIZATION, BEARER_PREFIX + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body));
    }

    /**
     * Creates an authenticated DELETE request.
     */
    public static MockHttpServletRequestBuilder createAuthenticatedDelete(
            String endpoint, 
            String token) {
        return MockMvcRequestBuilders.delete(endpoint)
            .header(AUTHORIZATION, BEARER_PREFIX + token);
    }
}