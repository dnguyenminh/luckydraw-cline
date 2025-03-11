package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import vn.com.fecredit.app.enums.RoleName;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String position;
        private Set<RoleName> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String position;
        private Set<RoleName> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String position;
        private Integer status;
        private Long version;
        private Set<RoleDTO.Summary> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private Set<RoleDTO.Summary> roles;
        private Integer status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePassword {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPassword {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Email is required")
        private String email;
    }
}
