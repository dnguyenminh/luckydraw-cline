package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import vn.com.fecredit.app.entity.User;

/**
 * DTO for user security information.
 * Used for authentication and authorization processes.
 * Implements UserDetails for Spring Security integration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityDTO implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean accountActive;
    private Set<String> roles;
    
    /**
     * Constructor that creates a UserSecurityDTO from a User entity
     * 
     * @param user The user entity to convert
     */
    public UserSecurityDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.accountActive = user.isEnabled();
        this.roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet());
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return accountActive;
    }
}