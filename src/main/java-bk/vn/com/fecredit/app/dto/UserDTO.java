package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import vn.com.fecredit.app.enums.RoleName;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class UserDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank
        @Size(min = 6, max = 40)
        private String password;

        @NotBlank
        @Size(max = 50)
        @Email
        private String email;

        @Size(max = 50)
        private String firstName;

        @Size(max = 50)
        private String lastName;

        @Size(max = 20)
        private String phoneNumber;

        @Builder.Default
        private EntityStatus status = EntityStatus.ACTIVE;

        @Builder.Default
        private Set<RoleName> roles = new HashSet<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 50)
        private String firstName;

        @Size(max = 50)
        private String lastName;

        @Size(max = 20)
        private String phoneNumber;

        private EntityStatus status;

        private Set<RoleName> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private EntityStatus status;
        private LocalDateTime lastLogin;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private Set<RoleDTO.Summary> roles;
        private String createdBy;
        private LocalDateTime createdAt;
        private String updatedBy;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private EntityStatus status;
        private Set<RoleName> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordChange {
        @NotBlank
        private String currentPassword;
        
        @NotBlank
        @Size(min = 6, max = 40)
        private String newPassword;
        
        @NotBlank
        @Size(min = 6, max = 40)
        private String confirmPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginInfo {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private EntityStatus status;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private Set<String> roles;
        private LocalDateTime lastLogin;
        private int failedAttempts;
        private LocalDateTime lockedUntil;
    }
}
