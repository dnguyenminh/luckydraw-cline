package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.enums.Permission;
import vn.com.fecredit.app.enums.RoleName;

@Entity
@Table(name = "roles")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractStatusAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

    @Column(length = 50, unique = true)
    private String code;

    @Column(length = 200)
    private String description;

    private Integer priority;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // Permission management
    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(permission);
    }

    public void addPermission(String permissionName) {
        try {
            Permission permission = Permission.fromName(permissionName);
            addPermission(permission);
        } catch (IllegalArgumentException e) {
            // Invalid permission name - silently ignore
        }
    }

    public void removePermission(Permission permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }

    public void removePermission(String permissionName) {
        try {
            Permission permission = Permission.fromName(permissionName);
            removePermission(permission);
        } catch (IllegalArgumentException e) {
            // Invalid permission name - silently ignore
        }
    }

    public boolean hasPermission(Permission permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName) {
        try {
            Permission permission = Permission.fromName(permissionName);
            return hasPermission(permission);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setPermissions(Set<String> permissionNames) {
        if (permissionNames == null) {
            this.permissions = new HashSet<>();
            return;
        }
        this.permissions = permissionNames.stream()
                .map(name -> {
                    try {
                        return Permission.fromName(name);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(p -> p != null)
                .collect(Collectors.toSet());
    }

    // User management
    public void addUser(User user) {
        if (users == null) {
            users = new HashSet<>();
        }
        users.add(user);
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(this);
    }

    public void removeUser(User user) {
        if (users != null) {
            users.remove(user);
            if (user.getRoles() != null) {
                user.getRoles().remove(this);
            }
        }
    }

    public boolean hasUser(User user) {
        return users != null && users.contains(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return getCode() != null && getCode().equals(role.getCode());
    }

    @Override
    public int hashCode() {
        return getCode() != null ? getCode().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Role(name=" + name + ", code=" + code + ")";
    }
}
