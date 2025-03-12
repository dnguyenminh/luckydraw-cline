package vn.com.fecredit.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.service.TokenBlacklistService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenBlacklistController.class)
class TokenBlacklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    private BlacklistedToken testToken;
    private List<BlacklistedToken> testTokens;
    private long currentTime;

    @BeforeEach
    void setUp() {
        currentTime = Instant.now().toEpochMilli();
        
        testToken = BlacklistedToken.builder()
                .id(1L)
                .tokenHash("test_hash")
                .expirationTime(currentTime + 3600000)
                .revokedBy("admin")
                .revocationReason("test")
                .refreshToken(false)
                .build();

        testTokens = Arrays.asList(
            testToken,
            BlacklistedToken.builder()
                .id(2L)
                .tokenHash("test_hash_2")
                .expirationTime(currentTime + 7200000)
                .revokedBy("admin")
                .revocationReason("test2")
                .refreshToken(true)
                .build()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getExpiringTokens_ShouldReturnTokens() throws Exception {
        when(tokenBlacklistService.getTokensExpiringBetween(anyLong(), anyLong()))
                .thenReturn(testTokens);

        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring")
                .param("hours", "24")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved expiring tokens"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].tokenHash").value("test_hash"));

        verify(tokenBlacklistService).getTokensExpiringBetween(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTokensByUser_ShouldReturnTokens() throws Exception {
        List<String> tokenHashes = Arrays.asList("hash1", "hash2");
        when(tokenBlacklistService.getTokensRevokedBy("admin"))
                .thenReturn(tokenHashes);

        mockMvc.perform(get("/api/v1/tokens/blacklist/by-user/admin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved tokens revoked by admin"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(tokenBlacklistService).getTokensRevokedBy("admin");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTokenCount_ShouldReturnCount() throws Exception {
        when(tokenBlacklistService.countByType(true)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/tokens/blacklist/count")
                .param("refreshTokens", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved token count"))
                .andExpect(jsonPath("$.data").value(5));

        verify(tokenBlacklistService).countByType(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeToken_WhenTokenExists_ShouldRemoveToken() throws Exception {
        when(tokenBlacklistService.removeFromBlacklist("test.token"))
                .thenReturn(true);

        mockMvc.perform(delete("/api/v1/tokens/blacklist/test.token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token removed from blacklist"));

        verify(tokenBlacklistService).removeFromBlacklist("test.token");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cleanupExpiredTokens_ShouldReturnCount() throws Exception {
        when(tokenBlacklistService.cleanupExpiredTokens()).thenReturn(3);

        mockMvc.perform(delete("/api/v1/tokens/blacklist/expired")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully cleaned up expired tokens"))
                .andExpect(jsonPath("$.data").value(3));

        verify(tokenBlacklistService).cleanupExpiredTokens();
    }

    @Test
    void whenUserNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenUserNotAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
