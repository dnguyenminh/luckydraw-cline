package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.auth.*;
import vn.com.fecredit.app.dto.common.DataResponse;
import vn.com.fecredit.app.entity.User;

public interface AuthService {

    /**
     * Authenticate user and generate token
     */
    LoginResponse login(LoginRequest request);

    /**
     * Register new user
     */
    LoginResponse register(RegisterRequest request);

    /**
     * Change user password
     */
    DataResponse<Void> changePassword(String username, ChangePasswordRequest request);

    /**
     * Request password reset
     */
    DataResponse<Void> forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password using token
     */
    DataResponse<Void> resetPassword(ResetPasswordRequest request);

    /**
     * Refresh access token using refresh token
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * Validate token
     */
    boolean validateToken(String token);

    /**
     * Get current authenticated user
     */
    User getCurrentUser();

    /**
     * Get user by username
     */
    User getUserByUsername(String username);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Logout current user
     */
    void logout();

    /**
     * Invalidate all tokens for user
     */
    void invalidateTokens(String username);

    /**
     * Get user details from token
     */
    LoginResponse.UserInfo getUserInfoFromToken(String token);

    /**
     * Block user account
     */
    void blockUser(String username, String reason);

    /**
     * Unblock user account
     */
    void unblockUser(String username);

    /**
     * Check if user account is blocked
     */
    boolean isUserBlocked(String username);
}
