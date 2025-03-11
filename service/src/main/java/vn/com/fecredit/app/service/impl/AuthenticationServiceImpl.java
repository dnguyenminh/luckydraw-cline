package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.AuthDTO;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.dto.UserSecurityDTO;
import vn.com.fecredit.app.dto.ApiResponse;
import vn.com.fecredit.app.mapper.UserMapper;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.AuthenticationService;
import vn.com.fecredit.app.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<AuthDTO.LoginResponse> login(AuthDTO.LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // Get user and create security wrapper
        UserDTO.Response user = userService.getByUsername(request.getUsername());
        UserSecurityDTO userDetails = new UserSecurityDTO(userMapper.fromResponse(user));

        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", true);
        String refreshToken = jwtService.generateToken(claims, userDetails);
        
        // Create response
        AuthDTO.LoginResponse response = AuthDTO.LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .user(userMapper.responseToSummary(user))
            .build();
            
        return ApiResponse.success(response, "Login successful");
    }

    @Override
    public ApiResponse<AuthDTO.LoginResponse> register(AuthDTO.RegisterRequest request) {
        // Convert to UserDTO.CreateRequest
        UserDTO.CreateRequest createRequest = UserDTO.CreateRequest.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .roles(request.getRoles())
            .build();

        // Create new user
        UserDTO.Response user = userService.create(createRequest);
        UserSecurityDTO userDetails = new UserSecurityDTO(userMapper.fromResponse(user));

        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", true);
        String refreshToken = jwtService.generateToken(claims, userDetails);

        // Create response
        AuthDTO.LoginResponse response = AuthDTO.LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .user(userMapper.responseToSummary(user))
            .build();

        return ApiResponse.success(response, "Registration successful");
    }

    @Override
    public ApiResponse<AuthDTO.LoginResponse> refreshToken(String refreshToken) {
        // Get user from token and validate
        String username = jwtService.extractUsername(refreshToken);
        if (username == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        UserDTO.Response user = userService.getByUsername(username);
        UserSecurityDTO userDetails = new UserSecurityDTO(userMapper.fromResponse(user));
        
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        // Generate new tokens
        String accessToken = jwtService.generateToken(userDetails);
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", true);
        String newRefreshToken = jwtService.generateToken(claims, userDetails);

        // Create response
        AuthDTO.LoginResponse response = AuthDTO.LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .user(userMapper.responseToSummary(user))
            .build();
            
        return ApiResponse.success(response, "Token refreshed");
    }

    @Override
    public ApiResponse<Void> logout(String accessToken) {
        String username = jwtService.extractUsername(accessToken);
        if (username != null) {
            UserDTO.Response user = userService.getByUsername(username);
            userService.updateStatus(user.getId(), 0); // Set token as invalidated
        }
        return ApiResponse.success(null, "Logged out successfully");
    }

    @Override
    public ApiResponse<Void> changePassword(AuthDTO.ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        String username = jwtService.extractCurrentUsername();
        UserDTO.Response user = userService.getByUsername(username);
        userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ApiResponse.success(null, "Password changed successfully");
    }

    @Override
    public ApiResponse<Void> requestPasswordReset(String email) {
        userService.requestPasswordReset(email);
        return ApiResponse.success(null, "Password reset email sent");
    }

    @Override
    public ApiResponse<Void> resetPassword(AuthDTO.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        userService.validatePasswordResetToken(request.getToken());
        UserDTO.Response user = userService.findActiveByEmail(request.getToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        userService.resetPassword(user.getId(), request.getToken(), request.getNewPassword());
        return ApiResponse.success(null, "Password reset successful");
    }

    @Override
    public ApiResponse<Void> validatePasswordResetToken(String token) {
        userService.validatePasswordResetToken(token);
        return ApiResponse.success(null, "Token is valid");
    }
}
