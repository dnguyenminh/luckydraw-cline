package vn.com.fecredit.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityIntegrationTest extends BaseIntegrationTest {

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Given
        ResetPasswordRequest.LoginRequest request = ResetPasswordRequest.LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andExpect(jsonPath("$.data.roles[0].name").value("USER"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        ResetPasswordRequest.LoginRequest request = ResetPasswordRequest.LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithInactiveUser_ShouldReturnForbidden() throws Exception {
        // Given
        ResetPasswordRequest.LoginRequest request = ResetPasswordRequest.LoginRequest.builder()
                .username("inactive")
                .password("inactive123")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        // Given
        ResetPasswordRequest.RegisterRequest request = ResetPasswordRequest.RegisterRequest.builder()
                .username("newuser")
                .password("Password123@")
                .email("newuser@example.com")
                .fullName("New User")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("newuser"));

        // Verify user creation
        User createdUser = userRepository.findByUsername("newuser")
                .orElseThrow(() -> new AssertionError("User not created"));
        assertThat(createdUser.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(createdUser.getRoles()).hasSize(1);
        assertThat(createdUser.getRoles().iterator().next().getName()).isEqualTo(RoleName.USER);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        // Given
        String refreshToken = getRefreshToken(RoleName.USER);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + refreshToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void refreshToken_WithBlacklistedToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        String refreshToken = getRefreshToken(RoleName.USER);

        blacklistedTokenRepository.save(BlacklistedToken.builder()
                .tokenHash(tokenBlacklistService.hashToken(refreshToken))
                .expirationTime(System.currentTimeMillis() + 3600000)
                .revokedBy("system")
                .revocationReason("test")
                .refreshToken(true)
                .build());

        // When/Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + refreshToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_WithValidToken_ShouldUpdatePassword() throws Exception {
        // Given
        ResetPasswordRequest.ChangePasswordRequest request = ResetPasswordRequest.ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newPassword123@")
                .confirmPassword("newPassword123@")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                .header("Authorization", getAuthHeader(RoleName.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk());

        // Verify password change
        User updatedUser = reloadEntity(userRepository.findByUsername("testuser").orElseThrow());
        assertThat(passwordEncoder.matches("newPassword123@", updatedUser.getPassword())).isTrue();
    }

    @Test
    void logout_WithValidToken_ShouldBlacklistToken() throws Exception {
        // Given
        String accessToken = testTokens.get("userAccessToken");

        // When
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify token blacklisting
        assertThat(blacklistedTokenRepository.existsByTokenHash(
            tokenBlacklistService.hashToken(accessToken))).isTrue();
    }

    @Test
    void protectedEndpoint_WithExpiredToken_ShouldReturnUnauthorized() throws Exception {
        // Given - Create an expired token
        User testUser = userRepository.findByUsername("testuser").orElseThrow();
        Map<String, Object> claims = new HashMap<>();
        claims.put("exp", System.currentTimeMillis() / 1000 - 3600);
        String expiredToken = jwtService.generateToken(claims, testUser);

        // When/Then
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
