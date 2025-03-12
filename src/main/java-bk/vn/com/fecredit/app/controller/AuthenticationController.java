package vn.com.fecredit.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.config.JwtProperties;
import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final JwtProperties jwtProperties;

    @Operation(summary = "Login", description = "Authenticate user and return tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/login")
    public ResponseEntity<ResetPasswordRequest.LoginResponse> login(
            @Valid @RequestBody ResetPasswordRequest.LoginRequest request,
            HttpServletResponse response) {
        ResetPasswordRequest.LoginResponse loginResponse = authService.login(request);
        addRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Register", description = "Register a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid request or user already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ResetPasswordRequest.LoginResponse> register(
            @Valid @RequestBody ResetPasswordRequest.RegisterRequest request,
            HttpServletResponse response) {
        ResetPasswordRequest.LoginResponse loginResponse = authService.register(request);
        addRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New tokens generated"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ResetPasswordRequest.LoginResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestBody(required = false) ResetPasswordRequest.RefreshTokenRequest request,
            HttpServletResponse response) {
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie :
                (request != null ? request.getRefreshToken() : null);

        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        ResetPasswordRequest.LoginResponse loginResponse = authService.refreshToken(refreshToken);
        addRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Logout", description = "Invalidate tokens and logout")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully logged out"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            authService.logout(accessToken);
        }
        removeRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password", description = "Change user's password")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Invalid current password")
    })
    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ResetPasswordRequest.ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Request password reset", description = "Send password reset email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reset email sent if email exists"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ResetPasswordRequest.ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password", description = "Reset password using token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(jwtProperties.isHttpOnly())
                .secure(jwtProperties.isSecure())
                .path(jwtProperties.getPath())
                .maxAge(jwtProperties.getRefreshExpiration() / 1000) // Convert to seconds
                .sameSite(jwtProperties.getSameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void removeRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(jwtProperties.isSecure())
                .path(jwtProperties.getPath())
                .maxAge(0)
                .sameSite(jwtProperties.getSameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
