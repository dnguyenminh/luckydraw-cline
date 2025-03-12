package vn.com.fecredit.app.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import vn.com.fecredit.app.exception.BusinessException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SecurityUtils {

    /**
     * Get the current authenticated username
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            throw new BusinessException("No authentication present");
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }

        return authentication.getName();
    }

    /**
     * Get current authenticated username as Optional
     */
    public static Optional<String> getCurrentUsernameOpt() {
        try {
            return Optional.of(getCurrentUsername());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get current user's roles
     */
    public static Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserRoles().contains(roleWithPrefix);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        Set<String> currentRoles = getCurrentUserRoles();
        for (String role : roles) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            if (currentRoles.contains(roleWithPrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is staff
     */
    public static boolean isStaff() {
        return hasRole("STAFF");
    }

    /**
     * Check if current user is participant
     */
    public static boolean isParticipant() {
        return hasRole("PARTICIPANT");
    }

    /**
     * Check if user has permission to manage entity
     */
    public static boolean canManageEntity(String username) {
        return isAdmin() || isStaff() || getCurrentUsername().equals(username);
    }

    /**
     * Get authentication token from header
     */
    public static Optional<String> extractTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(header.substring(7));
    }

    /**
     * Mask sensitive data for logging
     */
    public static String maskSensitiveData(String data) {
        if (data == null) {
            return null;
        }
        if (data.length() <= 8) {
            return "*".repeat(data.length());
        }
        return data.substring(0, 4) + "*".repeat(data.length() - 8) + data.substring(data.length() - 4);
    }
}
