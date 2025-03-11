package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.enums.RoleName;
import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void testRoleBuilder() {
        Role role = Role.builder()
                .name(RoleName.USER)
                .code("user_code")
                .description("User role")
                .priority(1)
                .build();

        assertThat(role.getName()).isEqualTo(RoleName.USER);
        assertThat(role.getCode()).isEqualTo("user_code");
        assertThat(role.getDescription()).isEqualTo("User role");
        assertThat(role.getPriority()).isEqualTo(1);
    }

    @Test
    void testHasPermission() {
        Role role = Role.builder()
                .name(RoleName.ADMIN)
                .build();

        role.addPermission("CREATE_USER");
        assertThat(role.hasPermission("CREATE_USER")).isTrue();
        assertThat(role.hasPermission("DELETE_USER")).isFalse();
    }
}
