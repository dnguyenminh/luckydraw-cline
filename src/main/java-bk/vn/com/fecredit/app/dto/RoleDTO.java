package vn.com.fecredit.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import vn.com.fecredit.app.enums.RoleName;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class RoleDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role creation request")
    public static class CreateRequest {
        @NotNull(message = "Role name is required")
        @Schema(description = "Role name", example = "ADMIN")
        private RoleName name;

        @Size(max = 200, message = "Description must not exceed 200 characters")
        @Schema(description = "Role description", example = "Administrator role with full access")
        private String description;

        @Builder.Default
        @Schema(description = "Role permissions")
        private Set<String> permissions = new HashSet<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role update request")
    public static class UpdateRequest {
        @Size(max = 200, message = "Description must not exceed 200 characters")
        @Schema(description = "Role description", example = "Updated role description")
        private String description;

        @Schema(description = "Role permissions")
        private Set<String> permissions;

        @Schema(description = "Role status")
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role response")
    public static class Response {
        @Schema(description = "Role ID", example = "1")
        private Long id;

        @Schema(description = "Role name", example = "ADMIN")
        private RoleName name;

        @Schema(description = "Role description", example = "Administrator role")
        private String description;

        @Schema(description = "Role permissions")
        private Set<String> permissions;

        @Schema(description = "Number of users with this role")
        private long userCount;

        @Schema(description = "Role creation timestamp")
        private LocalDateTime createdAt;

        @Schema(description = "Role creator")
        private String createdBy;

        @Schema(description = "Role last update timestamp")
        private LocalDateTime updatedAt;

        @Schema(description = "Role last updater")
        private String updatedBy;

        @Schema(description = "Role active status")
        private boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role summary")
    public static class Summary {
        @Schema(description = "Role ID", example = "1")
        private Long id;

        @Schema(description = "Role name", example = "ADMIN")
        private RoleName name;

        @Schema(description = "Role display name", example = "Administrator")
        private String displayName;

        @Schema(description = "Role description", example = "Administrator role")
        private String description;

        @Schema(description = "Number of users with this role")
        private long userCount;

        @Schema(description = "Role active status")
        private boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role permission update request")
    public static class PermissionUpdate {
        @NotBlank(message = "Permission name is required")
        @Schema(description = "Permission name", example = "READ_USERS")
        private String permission;

        @Schema(description = "Grant or revoke permission")
        private boolean grant;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Role assignment request")
    public static class Assignment {
        @NotNull(message = "User ID is required")
        @Schema(description = "User ID", example = "1")
        private Long userId;

        @NotNull(message = "Role name is required")
        @Schema(description = "Role name", example = "ADMIN")
        private RoleName roleName;
    }
}
