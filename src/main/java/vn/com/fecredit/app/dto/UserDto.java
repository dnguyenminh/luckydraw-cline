package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.enums.RoleName;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Set<RoleName> roles;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100)
        private String password;

        @Email(message = "Invalid email format")
        @Size(max = 100)
        private String email;

        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        private String fullName;

        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
        private String phone;

        private Set<RoleName> roles;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Email(message = "Invalid email format")
        @Size(max = 100)
        private String email;

        @Size(max = 100)
        private String fullName;

        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
        private String phone;

        private Set<RoleName> roles;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePassword {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 100)
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAuth {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private Set<RoleName> roles;
        private String token;
        private LocalDateTime tokenExpiration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private Set<RoleName> roles;
        private Boolean isActive;
        private Integer participationCount;
        private Integer totalSpins;
        private Integer winningSpins;
        private LocalDateTime lastActivityDate;
    }
}
