package vn.com.fecredit.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.ResultActions;
import vn.com.fecredit.app.dto.AuthResponse;
import vn.com.fecredit.app.dto.UserDto;
import vn.com.fecredit.app.util.assertions.AuthAssertions;
import vn.com.fecredit.app.util.assertions.UserDtoAssert;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static vn.com.fecredit.app.util.TestConstants.Auth.BEARER_PREFIX;

/**
 * Utility methods for validating test responses.
 */
public final class TestResponseUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TestResponseUtils() {
        // Prevent instantiation
    }

    /**
     * Extracts auth response from result.
     */
    public static AuthResponse extractAuthResponse(ResultActions resultActions) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, AuthResponse.class);
    }

    /**
     * Extracts token with Bearer prefix.
     */
    public static String extractTokenWithBearer(ResultActions resultActions) throws Exception {
        AuthResponse response = extractAuthResponse(resultActions);
        return BEARER_PREFIX + response.getToken();
    }

    /**
     * Validates successful authentication response.
     */
    public static void validateAuthResponse(ResultActions resultActions) throws Exception {
        // Validate HTTP status
        resultActions.andExpect(status().isOk())
            // Validate response structure
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user").exists())
            .andExpect(jsonPath("$.user.email").exists());

        // Extract and validate response object
        AuthResponse response = extractAuthResponse(resultActions);
        
        AuthAssertions.assertThat(response)
            .hasValidToken()
            .hasTokenType("Bearer")
            .hasValidUser();
    }

    /**
     * Validates unauthorized response.
     */
    public static void validateUnauthorizedResponse(ResultActions resultActions) throws Exception {
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.status").value(401));
    }

    /**
     * Validates forbidden response.
     */
    public static void validateForbiddenResponse(ResultActions resultActions) throws Exception {
        resultActions
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.status").value(403));
    }

    /**
     * Extracts and validates UserDto from response.
     */
    public static UserDto validateUserResponse(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk());
        String content = resultActions.andReturn().getResponse().getContentAsString();
        UserDto user = objectMapper.readValue(content, UserDto.class);
        
        UserDtoAssert.assertThat(user)
            .hasId(user.getId())
            .hasEmail(user.getEmail())
            .hasName(user.getFirstName(), user.getLastName());

        return user;
    }

    /**
     * Extracts token from authentication response.
     */
    public static String extractToken(ResultActions resultActions) throws Exception {
        AuthResponse response = extractAuthResponse(resultActions);
        return response.getToken();
    }
}