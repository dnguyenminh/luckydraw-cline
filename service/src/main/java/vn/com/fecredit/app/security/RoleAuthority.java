package vn.com.fecredit.app.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import vn.com.fecredit.app.enums.RoleName;

@Getter
@RequiredArgsConstructor
public class RoleAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;
    private static final String ROLE_PREFIX = "ROLE_";
    
    private final RoleName roleName;

    @Override
    public String getAuthority() {
        return ROLE_PREFIX + roleName.name();
    }

    public static RoleAuthority of(RoleName roleName) {
        return new RoleAuthority(roleName);
    }

    public static String toRoleString(RoleName roleName) {
        return ROLE_PREFIX + roleName.name();
    }

    public static RoleName fromRoleString(String roleString) {
        if (roleString == null || !roleString.startsWith(ROLE_PREFIX)) {
            throw new IllegalArgumentException("Invalid role string format: " + roleString);
        }
        return RoleName.valueOf(roleString.substring(ROLE_PREFIX.length()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleAuthority that = (RoleAuthority) o;
        return roleName == that.roleName;
    }

    @Override
    public int hashCode() {
        return roleName != null ? roleName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getAuthority();
    }
}
