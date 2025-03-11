package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "position")
    private String position;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "credentials_expired")
    @Builder.Default
    private boolean credentialsExpired = false;

    @Column(name = "account_expired")
    @Builder.Default
    private boolean accountExpired = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "refresh_token")
    private String refreshToken;

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return (firstName != null ? firstName : "") + 
               (lastName != null ? " " + lastName : "");
    }

    public boolean isEnabled() {
        Integer status = getStatus();
        return status != null && status.equals(STATUS_ACTIVE);
    }

    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && 
               lockedUntil.isAfter(LocalDateTime.now());
    }

    public void lockAccount(LocalDateTime until) {
        this.lockedUntil = until;
    }

    public void unlockAccount() {
        this.lockedUntil = null;
    }

    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    public void setAccountNonExpired(boolean nonExpired) {
        this.accountExpired = !nonExpired;
    }

    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    public void setCredentialsNonExpired(boolean nonExpired) {
        this.credentialsExpired = !nonExpired;
    }

    public boolean isAccountActive() {
        return isEnabled() && isAccountNonExpired() && isAccountNonLocked();
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Set<Role> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public void addRole(Role role) {
        if (role != null) {
            getRoles().add(role);
            role.getUsers().add(this);
        }
    }

    public void removeRole(Role role) {
        if (role != null) {
            getRoles().remove(role);
            role.getUsers().remove(this);
        }
    }

    public boolean hasRole(String roleName) {
        return getRoles().stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @PrePersist
    protected void onPrePersist() {
        Integer status = getStatus();
        if (status == null) {
            setStatus(STATUS_ACTIVE);
        }
    }
}
