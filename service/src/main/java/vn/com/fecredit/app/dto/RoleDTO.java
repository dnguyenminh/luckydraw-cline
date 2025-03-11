package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RoleDTO {
    private Long id;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer status;

    @NotBlank
    @Size(max = 20)
    private RoleName name;

    @NotBlank
    @Size(max = 50)
    private String code;

    @Size(max = 200)
    private String description;

    private Integer priority;
    
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        @Size(max = 20)
        private RoleName name;

        @NotBlank
        @Size(max = 50)
        private String code;

        @Size(max = 200)
        private String description;

        private Integer priority;
        
        @Builder.Default
        private Set<String> permissions = new HashSet<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 20)
        private RoleName name;

        @Size(max = 200)
        private String description;

        private Integer priority;
        
        @Builder.Default
        private Set<String> permissions = new HashSet<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assignment {
        @NotNull
        private Long userId;
        
        @NotNull
        private Long roleId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionUpdate {
        @NotNull
        private Set<String> permissions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private RoleName name;
        private String code;
        private String description;
        private Integer priority;
        private Integer status;
        private Set<String> permissions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private RoleName name;
        private String code;
        private String description;
        private Integer status;
        private Set<String> permissions;
    }
}
