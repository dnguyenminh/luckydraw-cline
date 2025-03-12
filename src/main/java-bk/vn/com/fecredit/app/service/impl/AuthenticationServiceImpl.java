package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.ApiResponse;
import vn.com.fecredit.app.dto.AuthDTO;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.RoleMapper;
import vn.com.fecredit.app.mapper.UserMapper;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.AuthenticationService;
import vn.com.fecredit.app.service.RoleService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public ApiResponse<AuthDTO.LoginResponse> login(AuthDTO.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateToken(Collections.singletonMap("refresh", true), user);

        return ApiResponse.success("Login successful", createLoginResponse(user, accessToken, refreshToken));
    }

    @Override
    @Transactional
    public ApiResponse<AuthDTO.LoginResponse> register(AuthDTO.RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new InvalidOperationException("Username already exists");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new InvalidOperationException("Email already exists");
        }

        Set<RoleName> roleNames = request.getRoles() != null && !request.getRoles().isEmpty() ?
                request.getRoles() :
                Collections.singleton(RoleName.USER);

        UserDTO.CreateRequest createRequest = UserDTO.CreateRequest.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(EntityStatus.ACTIVE)
                .roles(roleNames)
                .build();

        UserDTO.Response userResponse = userService.create(createRequest);
        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() -> new EntityNotFoundException("User", userResponse.getId()));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateToken(Collections.singletonMap("refresh", true), user);

        return ApiResponse.success("Registration successful", createLoginResponse(user, accessToken, refreshToken));
    }

    @Override
    public ApiResponse<AuthDTO.LoginResponse> refreshToken(String refreshToken) {
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidOperationException("Refresh token has been revoked");
        }

        if (!jwtService.isTokenValid(refreshToken, null)) {
            throw new InvalidOperationException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = (User) userService.loadUserByUsername(username);
        String accessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.refreshToken(refreshToken, user);

        Long expirationTime = jwtService.extractExpiration(refreshToken).getTime();
        tokenBlacklistService.blacklist(refreshToken, true, expirationTime, username, "Token refreshed");

        return ApiResponse.success("Token refreshed", createLoginResponse(user, accessToken, newRefreshToken));
    }

    @Override
    @Transactional
    public ApiResponse<Void> logout(String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            String username = jwtService.extractUsername(accessToken);
            Long expirationTime = jwtService.extractExpiration(accessToken).getTime();
            tokenBlacklistService.blacklist(accessToken, false, expirationTime, username, "User logout");
        }
        return ApiResponse.success("Logged out successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> changePassword(AuthDTO.ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }

        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        return ApiResponse.success("Password changed successfully");
    }

    @Override
    public ApiResponse<Void> requestPasswordReset(String email) {
        // Implement password reset email sending
        return ApiResponse.success("Password reset instructions sent to email");
    }

    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(AuthDTO.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }

        validatePasswordResetToken(request.getToken());
        String username = jwtService.extractUsername(request.getToken());
        User user = (User) userService.loadUserByUsername(username);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success("Password reset successfully");
    }

    @Override
    public ApiResponse<Void> validatePasswordResetToken(String token) {
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new InvalidOperationException("Password reset token has been revoked");
        }
        if (!jwtService.isTokenValid(token, null)) {
            throw new InvalidOperationException("Invalid or expired password reset token");
        }
        return ApiResponse.success("Token is valid");
    }

    private AuthDTO.LoginResponse createLoginResponse(User user, String accessToken, String refreshToken) {
        UserDTO.Summary userSummary = userMapper.toSummary(user);

        return AuthDTO.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getTokenRemainingValidityInMillis(accessToken))
                .user(userSummary)
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidOperationException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}
