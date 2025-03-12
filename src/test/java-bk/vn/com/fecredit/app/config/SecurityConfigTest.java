package vn.com.fecredit.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithAnonymousUser
    void publicEndpoints_ShouldBeAccessible() throws Exception {
        // Test Swagger UI access
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        // Test API docs access
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        // Test authentication endpoint access
        ResetPasswordRequest.LoginRequest loginRequest = ResetPasswordRequest.LoginRequest.builder()
                .username("test")
                .password("test")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void protectedEndpoints_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/tokens/blacklist/expiring"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());
    }

    @Test
    void jwt_WithValidToken_ShouldAllowAccess() throws Exception {
        // Given
        String validToken = "valid.jwt.token";
        User testUser = User.builder()
                .id(1L)
                .username("test")
                .status(EntityStatus.ACTIVE)
                .build();

        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn("test");
        when(userService.loadUserByUsername("test")).thenReturn(testUser);

        // When/Then
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isForbidden()); // Still forbidden because no ADMIN role

        mockMvc.perform(get("/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void jwt_WithInvalidToken_ShouldReturn401() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(false);

        // When/Then
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwt_WithMalformedToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer malformed.token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "malformed_header"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void csrf_ShouldBeDisabled() throws Exception {
        // If CSRF was enabled, this would fail without a CSRF token
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }
}
