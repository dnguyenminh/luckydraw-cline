package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.com.fecredit.app.dto.ApiResponse;
import vn.com.fecredit.app.dto.AuthDTO;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.mapper.RoleMapper;
import vn.com.fecredit.app.mapper.UserMapper;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.RoleService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private RoleService roleService;
    @Mock private UserMapper userMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenBlacklistService tokenBlacklistService;

    private AuthenticationServiceImpl authService;
    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthenticationServiceImpl(
            authenticationManager, jwtService, userService, userRepository,
            roleService, userMapper, roleMapper, passwordEncoder, tokenBlacklistService
        );

        userRole = Role.builder()
                .name(RoleName.USER)
                .status(EntityStatus.ACTIVE)
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .status(EntityStatus.ACTIVE)
                .build();
        testUser.setRoles(Collections.singleton(userRole));
    }

    @Test
    void login_ShouldReturnLoginResponse() {
        // Given
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        UserDTO.Summary userSummary = UserDTO.Summary.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(EntityStatus.ACTIVE)
                .roles(Collections.singleton(RoleName.USER))
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(testUser, null));
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.generateToken(anyMap(), any())).thenReturn("refresh");
        when(jwtService.getTokenRemainingValidityInMillis(anyString())).thenReturn(3600000L);
        when(userMapper.toSummary(any())).thenReturn(userSummary);

        // When
        ApiResponse<AuthDTO.LoginResponse> response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).satisfies(loginResponse -> {
            assertThat(loginResponse.getAccessToken()).isEqualTo("token");
            assertThat(loginResponse.getRefreshToken()).isEqualTo("refresh");
            assertThat(loginResponse.getUser()).isNotNull();
            assertThat(loginResponse.getUser().getUsername()).isEqualTo("testuser");
        });
    }

    @Test
    void refreshToken_ShouldReturnNewTokens() {
        // Given
        String refreshToken = "valid_refresh_token";
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn("testuser");
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(jwtService.generateToken(any())).thenReturn("new_token");
        when(jwtService.refreshToken(anyString(), any())).thenReturn("new_refresh");
        when(jwtService.extractExpiration(anyString())).thenReturn(new Date());
        when(userMapper.toSummary(any())).thenReturn(mock(UserDTO.Summary.class));

        // When
        ApiResponse<AuthDTO.LoginResponse> response = authService.refreshToken(refreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).satisfies(loginResponse -> {
            assertThat(loginResponse.getAccessToken()).isEqualTo("new_token");
            assertThat(loginResponse.getRefreshToken()).isEqualTo("new_refresh");
        });
    }

    @Test
    void changePassword_ShouldSucceed() {
        // Given
        AuthDTO.ChangePasswordRequest request = AuthDTO.ChangePasswordRequest.builder()
                .currentPassword("current")
                .newPassword("newpass")
                .confirmPassword("newpass")
                .build();

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_new_password");
        when(userRepository.save(any())).thenReturn(testUser);

        // When
        ApiResponse<Void> response = authService.changePassword(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        verify(userRepository).save(any(User.class));
    }
}
