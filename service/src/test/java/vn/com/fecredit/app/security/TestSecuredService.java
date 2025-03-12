package vn.com.fecredit.app.security;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TestSecuredService {

    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnlyMethod() {
        return "Admin access granted";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String managerOrAdminMethod() {
        return "Manager/Admin access granted";
    }

    @PreAuthorize("hasRole('USER')")
    public String userOnlyMethod() {
        return "User access granted";
    }

    @PreAuthorize("isAuthenticated()")
    public String authenticatedOnlyMethod() {
        return "Authenticated access granted";
    }

    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    public String multipleRolesRequired() {
        return "Multiple roles access granted";
    }

    @PreAuthorize("#username == authentication.principal.username")
    public String selfAccessOnly(String username) {
        return "Self access granted for: " + username;
    }

    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public String adminOrSelfAccess(String username) {
        return "Admin or self access granted for: " + username;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostAuthorize("returnObject.contains(authentication.principal.username)")
    public String postAuthorizedMethod() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return "Access granted for: " + currentUser;
    }

    @PreAuthorize("@testSecurityConfig.getTestUser(#username) != null")
    public String customSecurityCheck(String username) {
        return "Custom security check passed for: " + username;
    }

    @PreAuthorize("hasRole('ADMIN') and (#amount >= 0 and #amount <= 1000)")
    public String methodWithParameterValidation(double amount) {
        return "Parameter validation passed for amount: " + amount;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostAuthorize("returnObject.startsWith('Admin')")
    public String methodWithPreAndPostAuthorization() {
        return "Admin operation completed";
    }

    @PreAuthorize("hasRole('USER') and @testSecurityConfig.getTestUser(authentication.name) != null")
    public String methodWithBeanReference() {
        return "Bean reference validation passed";
    }
}
