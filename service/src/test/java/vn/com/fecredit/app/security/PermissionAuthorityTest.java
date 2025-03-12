package vn.com.fecredit.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vn.com.fecredit.app.enums.Permission;

class PermissionAuthorityTest {

    @Nested
    class CreationTests {
        @ParameterizedTest
        @EnumSource(Permission.class)
        void shouldCreateFromPermissionEnum(Permission permission) {
            PermissionAuthority authority = PermissionAuthority.of(permission);
            assertThat(authority.getAuthority()).isEqualTo("PERMISSION_" + permission.name());
            assertThat(authority.getPermission()).isEqualTo(permission);
        }

        @Test
        void shouldCreateFromValidPermissionString() {
            PermissionAuthority authority = PermissionAuthority.of("USER_CREATE");
            assertThat(authority.getPermission()).isEqualTo(Permission.USER_CREATE);
        }

        @Test
        void shouldThrowExceptionForInvalidPermissionString() {
            assertThatThrownBy(() -> PermissionAuthority.of("INVALID_PERMISSION"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid permission name");
        }
    }

    @Nested
    class StringConversionTests {
        @Test
        void shouldConvertToPermissionString() {
            String permissionString = PermissionAuthority.toPermissionString(Permission.USER_CREATE);
            assertThat(permissionString).isEqualTo("PERMISSION_USER_CREATE");
        }

        @Test
        void shouldParseValidPermissionString() {
            Permission permission = PermissionAuthority.fromPermissionString("PERMISSION_USER_CREATE");
            assertThat(permission).isEqualTo(Permission.USER_CREATE);
        }

        @Test
        void shouldRejectInvalidPermissionString() {
            assertThatThrownBy(() -> PermissionAuthority.fromPermissionString("INVALID_FORMAT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid permission string format");
        }
    }

    @Nested
    class ImplicationTests {
        @Test
        void systemAdminShouldImplyAllPermissions() {
            PermissionAuthority adminAuth = PermissionAuthority.of(Permission.SYSTEM_ADMIN);
            
            for (Permission permission : Permission.values()) {
                PermissionAuthority otherAuth = PermissionAuthority.of(permission);
                assertThat(adminAuth.implies(otherAuth))
                    .as("SYSTEM_ADMIN should imply " + permission)
                    .isTrue();
            }
        }

        @Test
        void userCreateShouldImplyUserRead() {
            PermissionAuthority createAuth = PermissionAuthority.of(Permission.USER_CREATE);
            PermissionAuthority readAuth = PermissionAuthority.of(Permission.USER_READ);
            
            assertThat(createAuth.implies(readAuth)).isTrue();
        }

        @Test
        void userPermissionsShouldNotImplyRolePermissions() {
            PermissionAuthority userAuth = PermissionAuthority.of(Permission.USER_CREATE);
            PermissionAuthority roleAuth = PermissionAuthority.of(Permission.ROLE_CREATE);
            
            assertThat(userAuth.implies(roleAuth)).isFalse();
        }
    }

    @Nested
    class EqualityTests {
        @Test
        void samePermissionsShouldBeEqual() {
            PermissionAuthority auth1 = PermissionAuthority.of(Permission.USER_CREATE);
            PermissionAuthority auth2 = PermissionAuthority.of(Permission.USER_CREATE);

            assertThat(auth1)
                .isEqualTo(auth2)
                .hasSameHashCodeAs(auth2);
        }

        @Test
        void differentPermissionsShouldNotBeEqual() {
            PermissionAuthority auth1 = PermissionAuthority.of(Permission.USER_CREATE);
            PermissionAuthority auth2 = PermissionAuthority.of(Permission.USER_READ);

            assertThat(auth1)
                .isNotEqualTo(auth2)
                .isNotEqualTo(null)
                .isNotEqualTo(new Object());
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void shouldProvideReadableString() {
            PermissionAuthority authority = PermissionAuthority.of(Permission.USER_CREATE);
            assertThat(authority.toString()).isEqualTo("PERMISSION_USER_CREATE");
        }

        @ParameterizedTest
        @EnumSource(Permission.class)
        void shouldMatchAuthorityString(Permission permission) {
            PermissionAuthority authority = PermissionAuthority.of(permission);
            assertThat(authority.toString()).isEqualTo(authority.getAuthority());
        }
    }
}
