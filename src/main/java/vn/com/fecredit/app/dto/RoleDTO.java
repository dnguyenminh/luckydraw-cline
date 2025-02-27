package vn.com.fecredit.app.dto;

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
    private RoleName name;
    private String description;
    private Integer userCount;
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Role name is required")
        private RoleName name;

        @Size(max = 200)
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 200)
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private RoleName name;
        private String description;
        private Integer userCount;
        private Long version;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private RoleName name;
        private Integer userCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assignment {
        @NotNull(message = "User ID is required")
        private Long userId;
        
        @NotNull(message = "Role names are required")
        private java.util.Set<RoleName> roleNames;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Privileges {
        private Long id;
        private RoleName name;
        private java.util.Set<String> permissions;
        private Integer level;
        private Boolean isSystem;
    }
}
