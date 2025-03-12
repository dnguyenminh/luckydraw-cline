package vn.com.fecredit.app.util.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.IterableAssert;
import vn.com.fecredit.app.model.User;
import vn.com.fecredit.app.model.Role;

import java.util.Set;

/**
 * Custom assertions for User entity validation.
 */
public class UserAssert extends AbstractAssert<UserAssert, User> {

    private UserAssert(User actual) {
        super(actual, UserAssert.class);
    }

    public static UserAssert assertThat(User actual) {
        return new UserAssert(actual);
    }

    /**
     * Verifies the user ID matches expected value.
     */
    public UserAssert hasId(Long expectedId) {
        isNotNull();
        new ObjectAssert<>(actual.getId())
            .isEqualTo(expectedId);
        return this;
    }

    /**
     * Verifies the user email matches expected value.
     */
    public UserAssert hasEmail(String expectedEmail) {
        isNotNull();
        new StringAssert(actual.getEmail())
            .isEqualTo(expectedEmail);
        return this;
    }

    /**
     * Verifies the user's first and last name.
     */
    public UserAssert hasName(String expectedFirstName, String expectedLastName) {
        isNotNull();
        new StringAssert(actual.getFirstName()).isEqualTo(expectedFirstName);
        new StringAssert(actual.getLastName()).isEqualTo(expectedLastName);
        return this;
    }

    /**
     * Verifies the user has specific roles.
     */
    public UserAssert hasRoles(Set<Role> expectedRoles) {
        isNotNull();
        new IterableAssert<>(actual.getRoles())
            .containsExactlyInAnyOrderElementsOf(expectedRoles);
        return this;
    }

    /**
     * Verifies the user is enabled.
     */
    public UserAssert isEnabled() {
        isNotNull();
        new BooleanAssert(actual.isEnabled())
            .isTrue();
        return this;
    }
}