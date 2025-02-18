package vn.com.fecredit.app.util.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.api.ObjectAssert;
import vn.com.fecredit.app.model.Role;

/**
 * Custom assertions for Role validation.
 */
public class RoleAssert extends AbstractAssert<RoleAssert, Role> {

    public RoleAssert(Role actual) {
        super(actual, RoleAssert.class);
    }

    public static RoleAssert assertThat(Role actual) {
        return new RoleAssert(actual);
    }

    /**
     * Verifies the role ID matches expected value.
     */
    public RoleAssert hasId(Long expectedId) {
        isNotNull();
        new ObjectAssert<>(actual.getId())
            .isEqualTo(expectedId);
        return this;
    }

    /**
     * Verifies the role name matches expected value.
     */
    public RoleAssert hasName(String expectedName) {
        isNotNull();
        new StringAssert(actual.getName())
            .isEqualTo(expectedName);
        return this;
    }

    /**
     * Verifies the role is a system role.
     */
    public RoleAssert isSystemRole() {
        isNotNull();
        new StringAssert(actual.getName())
            .startsWith("ROLE_");
        return this;
    }
}