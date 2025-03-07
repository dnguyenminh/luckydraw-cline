package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;


    @Column(name = "position")
    private String position;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "metadata")
    private String metadata;

//    @Column(name = "enabled")
//    @Builder.Default
//    private boolean enabled = true;

    @Column(name = "account_non_locked")
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "account_non_expired")
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired")
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Column(name = "failed_attempts")
    private int failedAttempts;

    @Column(name = "account_locked")
    private boolean accountLocked;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_expired")
    private boolean passwordExpired;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Participant participant;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Initialize with active status
    {
        setStatus(STATUS_ACTIVE);
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    public String getStatusName() {
        return status == STATUS_ACTIVE ? "Active" : "Inactive";
    }

    public boolean isActive() {
        return status == STATUS_ACTIVE;
    }

    public boolean isAccountActive() {
        return isActive() && accountNonLocked && accountNonExpired && credentialsNonExpired;
    }

    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.isActive() && r.getName().equals(roleName));
    }

//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//    }




//    public String getFullName() {
//        StringBuilder sb = new StringBuilder();
//        if (firstName != null) {
//            sb.append(firstName);
//        }
//        if (lastName != null) {
//            if (sb.length() > 0) {
//                sb.append(" ");
//            }
//            sb.append(lastName);
//        }
//        return sb.length() > 0 ? sb.toString() : null;
//    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.accountLocked = false;
        this.lockedUntil = null;
    }

    public void lockAccount(LocalDateTime lockedUntil) {
        this.accountLocked = true;
        this.lockedUntil = lockedUntil;
        this.accountNonLocked = false;
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.lockedUntil = null;
        this.accountNonLocked = true;
        resetFailedAttempts();
    }

    public boolean isAccountLocked() {
        if (!accountLocked) return false;
        if (lockedUntil == null) return true;
        return LocalDateTime.now().isBefore(lockedUntil);
    }



}
