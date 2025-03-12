package vn.com.fecredit.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import vn.com.fecredit.app.enums.RoleName;

class RoleAuthorityTest {

    private static final String ROLE_PREFIX = "ROLE_";

    @ParameterizedTest
    @EnumSource(RoleName.class)
    void testRoleAuthorityCreation(RoleName roleName) {
        RoleAuthority authority = RoleAuthority.of(roleName);
        assertThat(authority.getAuthority()).isEqualTo(ROLE_PREFIX + roleName.name());
        assertThat(authority.getRoleName()).isEqualTo(roleName);
    }

    @Test
    void testRoleStringConversion() {
        RoleName roleName = RoleName.ADMIN;
        String roleString = RoleAuthority.toRoleString(roleName);
        assertThat(roleString).isEqualTo(ROLE_PREFIX + roleName.name());
    }

    @Test
    void testFromValidRoleString() {
        String roleString = ROLE_PREFIX + RoleName.ADMIN.name();
        RoleName roleName = RoleAuthority.fromRoleString(roleString);
        assertThat(roleName).isEqualTo(RoleName.ADMIN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ROLE_INVALID", "", "null"})
    void testFromInvalidRoleString(String invalidRole) {
        assertThatThrownBy(() -> RoleAuthority.fromRoleString(invalidRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role string format");
    }

    @Test
    void testFromNullRoleString() {
        assertThatThrownBy(() -> RoleAuthority.fromRoleString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role string format");
    }

    @Test
    void testEquality() {
        RoleAuthority admin1 = RoleAuthority.of(RoleName.ADMIN);
        RoleAuthority admin2 = RoleAuthority.of(RoleName.ADMIN);
        RoleAuthority user = RoleAuthority.of(RoleName.USER);

        assertThat(admin1)
                .isEqualTo(admin2)
                .hasSameHashCodeAs(admin2)
                .isNotEqualTo(user)
                .isNotEqualTo(null)
                .isNotEqualTo(new Object());

        assertThat(admin1.hashCode()).isNotEqualTo(user.hashCode());
    }

    @Test
    void testToString() {
        RoleAuthority admin = RoleAuthority.of(RoleName.ADMIN);
        assertThat(admin.toString()).isEqualTo(ROLE_PREFIX + RoleName.ADMIN.name());
    }

    @Test
    void testRoleStringFormat() {
        for (RoleName role : RoleName.values()) {
            String roleString = RoleAuthority.toRoleString(role);
            assertThat(roleString)
                    .startsWith(ROLE_PREFIX)
                    .endsWith(role.name())
                    .hasSize(ROLE_PREFIX.length() + role.name().length());
        }
    }

    @Test
    void testRoleConversionRoundTrip() {
        for (RoleName originalRole : RoleName.values()) {
            String roleString = RoleAuthority.toRoleString(originalRole);
            RoleName convertedRole = RoleAuthority.fromRoleString(roleString);
            assertThat(convertedRole).isEqualTo(originalRole);
        }
    }
}
