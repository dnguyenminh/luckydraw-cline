package vn.com.fecredit.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.enums.RoleName;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TokenBlacklistIntegrationTest extends BaseIntegrationTest {

    @Test
    void getExpiringTokens_AsAdmin_ShouldReturnTokens() throws Exception {
        // When
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring")
                .header("Authorization", getAuthHeader(RoleName.ADMIN))
                .param("hours", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tokenHash").value("active_token_hash"));

        // Verify database state
        List<BlacklistedToken> tokens = blacklistedTokenRepository.findTokensExpiringBetween(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli() + 3600000
        );
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getTokenHash()).isEqualTo("active_token_hash");
    }

    @Test
    void getTokensByUser_AsAdmin_ShouldReturnUserTokens() throws Exception {
        // When
        mockMvc.perform(get("/api/v1/tokens/blacklist/by-user/testadmin")
                .header("Authorization", getAuthHeader(RoleName.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("active_token_hash"));

        // Verify database state
        List<String> tokens = blacklistedTokenRepository.findTokenHashesRevokedBy("testadmin");
        assertThat(tokens).hasSize(2); // active and expired tokens
    }

    @Test
    void removeToken_AsAdmin_ShouldDeleteToken() throws Exception {
        // Given
        String tokenToRemove = "active_token_hash";

        // When
        mockMvc.perform(delete("/api/v1/tokens/blacklist/{token}", tokenToRemove)
                .header("Authorization", getAuthHeader(RoleName.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token removed from blacklist"));

        // Verify database state
        assertThat(blacklistedTokenRepository.existsByTokenHash(tokenToRemove)).isFalse();
    }

    @Test
    void cleanupExpiredTokens_AsAdmin_ShouldRemoveExpiredTokens() throws Exception {
        // When
        mockMvc.perform(delete("/api/v1/tokens/blacklist/expired")
                .header("Authorization", getAuthHeader(RoleName.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value(1)); // One expired token

        // Verify database state
        assertThat(blacklistedTokenRepository.existsByTokenHash("expired_token_hash")).isFalse();
        assertThat(blacklistedTokenRepository.existsByTokenHash("active_token_hash")).isTrue();
    }

    @Test
    void countTokensByType_AsAdmin_ShouldReturnCorrectCounts() throws Exception {
        // Test access tokens count
        mockMvc.perform(get("/api/v1/tokens/blacklist/count")
                .header("Authorization", getAuthHeader(RoleName.ADMIN))
                .param("refreshTokens", "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1)); // One access token

        // Test refresh tokens count
        mockMvc.perform(get("/api/v1/tokens/blacklist/count")
                .header("Authorization", getAuthHeader(RoleName.ADMIN))
                .param("refreshTokens", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1)); // One refresh token
    }

    @Test
    void endpoints_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring")
                .header("Authorization", getAuthHeader(RoleName.USER))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/tokens/blacklist/by-user/testuser")
                .header("Authorization", getAuthHeader(RoleName.USER))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpoints_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/tokens/blacklist/by-user/testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
