package vn.com.fecredit.app.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import vn.com.fecredit.app.enums.Permission;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class PermissionAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;
    private static final String PERMISSION_PREFIX = "PERMISSION_";
    
    private final Permission permission;

    @Override
    public String getAuthority() {
        return PERMISSION_PREFIX + permission.name();
    }

    public static PermissionAuthority of(Permission permission) {
        return new PermissionAuthority(permission);
    }

    public static PermissionAuthority of(String permissionName) {
        return new PermissionAuthority(Permission.fromName(permissionName));
    }

    public static String toPermissionString(Permission permission) {
        return PERMISSION_PREFIX + permission.name();
    }

    public static Permission fromPermissionString(String permissionString) {
        if (permissionString == null || !permissionString.startsWith(PERMISSION_PREFIX)) {
            throw new IllegalArgumentException("Invalid permission string format: " + permissionString);
        }
        return Permission.fromName(permissionString.substring(PERMISSION_PREFIX.length()));
    }

    public boolean implies(PermissionAuthority other) {
        if (this.permission == Permission.SYSTEM_ADMIN) {
            return true; // SYSTEM_ADMIN implies all permissions
        }

        // Check if this permission implies the other permission based on entity type
        if (this.permission.name().startsWith("USER_") && other.permission.name().startsWith("USER_")) {
            return this.permission.ordinal() <= other.permission.ordinal();
        }
        if (this.permission.name().startsWith("ROLE_") && other.permission.name().startsWith("ROLE_")) {
            return this.permission.ordinal() <= other.permission.ordinal();
        }
        if (this.permission.name().startsWith("EVENT_") && other.permission.name().startsWith("EVENT_")) {
            return this.permission.ordinal() <= other.permission.ordinal();
        }
        if (this.permission.name().startsWith("DATA_") && other.permission.name().startsWith("DATA_")) {
            return this.permission.ordinal() <= other.permission.ordinal();
        }
        if (this.permission.name().startsWith("REPORT_") && other.permission.name().startsWith("REPORT_")) {
            return this.permission.ordinal() <= other.permission.ordinal();
        }

        return this.permission == other.permission;
    }

    @Override
    public String toString() {
        return getAuthority();
    }
}
