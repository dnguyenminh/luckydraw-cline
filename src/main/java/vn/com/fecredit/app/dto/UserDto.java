package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for User entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String department;
    private String position;
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String lastModifiedBy;
    private Integer version;

    /**
     * Checks if user has a specific role.
     */
    public boolean hasRole(String roleName) {
        return roles.contains(roleName);
    }

    /**
     * Adds a role to the user.
     */
    public void addRole(String role) {
        roles.add(role);
    }

    /**
     * Removes a role from the user.
     */
    public void removeRole(String role) {
        roles.remove(role);
    }

    /**
     * Gets user's account status.
     */
    public boolean isActive() {
        return enabled && accountNonLocked && accountNonExpired && credentialsNonExpired;
    }

    /**
     * Creates a builder with default values.
     */
    public static UserDtoBuilder defaultBuilder() {
        return builder()
            .enabled(true)
            .accountNonLocked(true)
            .accountNonExpired(true)
            .credentialsNonExpired(true)
            .roles(new HashSet<>());
    }
}