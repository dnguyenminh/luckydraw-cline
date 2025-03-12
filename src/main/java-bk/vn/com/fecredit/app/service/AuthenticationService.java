package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.AuthDTO;
import vn.com.fecredit.app.dto.ApiResponse;

public interface AuthenticationService {
    ApiResponse<AuthDTO.LoginResponse> login(AuthDTO.LoginRequest request);
    ApiResponse<AuthDTO.LoginResponse> register(AuthDTO.RegisterRequest request);
    ApiResponse<AuthDTO.LoginResponse> refreshToken(String refreshToken);
    ApiResponse<Void> logout(String accessToken);
    ApiResponse<Void> changePassword(AuthDTO.ChangePasswordRequest request);
    ApiResponse<Void> requestPasswordReset(String email);
    ApiResponse<Void> resetPassword(AuthDTO.ResetPasswordRequest request);
    ApiResponse<Void> validatePasswordResetToken(String token);
}
