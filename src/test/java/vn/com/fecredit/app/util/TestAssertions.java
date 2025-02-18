package vn.com.fecredit.app.util;

import org.assertj.core.api.AbstractAssert;
import vn.com.fecredit.app.dto.UserDto;
import vn.com.fecredit.app.model.Role;
import vn.com.fecredit.app.model.User;
import vn.com.fecredit.app.util.assertions.RoleAssert;

import static org.assertj.core.api.Assertions.assertThat;

public final class TestAssertions {
    
    private TestAssertions() {}

    public static UserAssert assertUser(User actual) {
        return new UserAssert(actual);
    }

    public static UserDtoAssert assertUserDto(UserDto actual) {
        return new UserDtoAssert(actual);
    }

    public static RoleAssert assertRole(Role actual) {
        return new RoleAssert(actual);
    }

    public static class UserAssert extends AbstractAssert<UserAssert, User> {
        
        public UserAssert(User actual) {
            super(actual, UserAssert.class);
        }

        public UserAssert hasBasicUserInfo(String username, String email) {
            isNotNull();
            assertThat(actual.getUsername()).isEqualTo(username);
            assertThat(actual.getEmail()).isEqualTo(email);
            return this;
        }

        public UserAssert hasAuditInfo() {
            isNotNull();
            assertThat(actual.getCreatedAt()).isNotNull();
            assertThat(actual.getUpdatedAt()).isNotNull();
            assertThat(actual.getCreatedBy()).isNotNull();
            assertThat(actual.getLastModifiedBy()).isNotNull();
            return this;
        }

        public UserAssert hasRole(String roleName) {
            isNotNull();
            assertThat(actual.getRoles())
                .extracting("name")
                .contains(roleName);
            return this;
        }
    }


    public static class UserDtoAssert extends AbstractAssert<UserDtoAssert, UserDto> {
        
        public UserDtoAssert(UserDto actual) {
            super(actual, UserDtoAssert.class);
        }

        public UserDtoAssert hasBasicUserInfo(String username, String email) {
            isNotNull();
            
            assertThat(actual.getUsername())
                .as("username")
                .isEqualTo(username);
                
            assertThat(actual.getEmail())
                .as("email")
                .isEqualTo(email);
                
            return this;
        }

        public UserDtoAssert hasRole(String roleName) {
            isNotNull();
            
            assertThat(actual.getRoles())
                .as("user roles")
                .contains(roleName);
                
            return this;
        }
    }
}