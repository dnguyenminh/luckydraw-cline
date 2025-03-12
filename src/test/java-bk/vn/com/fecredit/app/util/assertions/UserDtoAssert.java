package vn.com.fecredit.app.util.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.IterableAssert;
import vn.com.fecredit.app.dto.UserDto;

import java.util.Set;

/**
 * Custom assertions for UserDto validation.
 */
public class UserDtoAssert extends AbstractAssert<UserDtoAssert, UserDto> {

    private UserDtoAssert(UserDto actual) {
        super(actual, UserDtoAssert.class);
    }

    public static UserDtoAssert assertThat(UserDto actual) {
        return new UserDtoAssert(actual);
    }

    /**
     * Verifies the user ID matches expected value.
     */
    public UserDtoAssert hasId(Long expectedId) {
        isNotNull();
        new ObjectAssert<>(actual.getId())
            .isEqualTo(expectedId);
        return this;
    }

    /**
     * Verifies the user email matches expected value.
     */
    public UserDtoAssert hasEmail(String expectedEmail) {
        isNotNull();
        new StringAssert(actual.getEmail())
            .isEqualTo(expectedEmail);
        return this;
    }

    /**
     * Verifies the user's first and last name.
     */
    public UserDtoAssert hasName(String expectedFirstName, String expectedLastName) {
        isNotNull();
        new StringAssert(actual.getFirstName()).isEqualTo(expectedFirstName);
        new StringAssert(actual.getLastName()).isEqualTo(expectedLastName);
        return this;
    }

    /**
     * Verifies the user has the expected roles.
     */
    public UserDtoAssert hasRoles(Set<String> expectedRoles) {
        isNotNull();
        new IterableAssert<>(actual.getRoles())
            .containsExactlyInAnyOrderElementsOf(expectedRoles);
        return this;
    }
}