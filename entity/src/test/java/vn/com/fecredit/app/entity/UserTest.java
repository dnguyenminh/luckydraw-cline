package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UserTest extends BaseEntityTest {

    private User user;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser_" + generateUniqueCode());
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setPosition("Developer");
//        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setStatus(User.STATUS_ACTIVE);
        user.setRoles(new HashSet<>());
        user.setMetadata(generateMetadata("user"));

        adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setStatus(Role.STATUS_ACTIVE);
        adminRole.setUsers(new HashSet<>());

        userRole = new Role();
        userRole.setName("USER");
        userRole.setStatus(Role.STATUS_ACTIVE);
        userRole.setUsers(new HashSet<>());
    }

    @Test
    void hasRole_WhenUserHasActiveRole_ShouldReturnTrue() {
        adminRole.setStatus(Role.STATUS_ACTIVE);
        user.getRoles().add(adminRole);

        assertTrue(user.hasRole("ADMIN"));
    }

    @Test
    void hasRole_WhenUserHasInactiveRole_ShouldReturnFalse() {
        adminRole.setStatus(Role.STATUS_INACTIVE);
        user.getRoles().add(adminRole);

        assertFalse(user.hasRole("ADMIN"));
    }

    @Test
    void hasRole_WhenUserDoesNotHaveRole_ShouldReturnFalse() {
        assertFalse(user.hasRole("ADMIN"));
    }

    @Test
    void addRole_ShouldSetBidirectionalRelationship() {
        user.addRole(adminRole);

        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(adminRole.getUsers().contains(user));
    }

    @Test
    void removeRole_ShouldRemoveBidirectionalRelationship() {
        user.addRole(adminRole);
        user.removeRole(adminRole);

        assertFalse(user.getRoles().contains(adminRole));
        assertFalse(adminRole.getUsers().contains(user));
    }

    @Test
    void isAccountActive_WhenAllFlagsAreTrue_ShouldReturnTrue() {
//        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setStatus(User.STATUS_ACTIVE);

        assertTrue(user.isAccountActive());
    }

    @Test
    void isAccountActive_WhenDisabled_ShouldReturnFalse() {
        user.setStatus(User.STATUS_INACTIVE);
        assertFalse(user.isAccountActive());
    }

    @Test
    void isAccountActive_WhenLocked_ShouldReturnFalse() {
        user.setAccountNonLocked(false);
        assertFalse(user.isAccountActive());
    }

    @Test
    void isAccountActive_WhenExpired_ShouldReturnFalse() {
        user.setAccountNonExpired(false);
        assertFalse(user.isAccountActive());
    }

    @Test
    void isAccountActive_WhenCredentialsExpired_ShouldReturnFalse() {
        user.setCredentialsNonExpired(false);
        assertFalse(user.isAccountActive());
    }

    @Test
    void isAccountActive_WhenStatusNotActive_ShouldReturnFalse() {
        user.setStatus(User.STATUS_INACTIVE);
        assertFalse(user.isAccountActive());
    }

    @Test
    void refreshToken_ShouldBeStoredAndRetrieved() {
        String token = "refresh.token." + generateUniqueCode();
        user.setRefreshToken(token);
        assertEquals(token, user.getRefreshToken());
    }

    @Test
    void defaultValues_ShouldBeSetCorrectly() {
        User newUser = new User();
        assertEquals(newUser.getStatus(), User.STATUS_ACTIVE);
        assertTrue(newUser.isAccountNonLocked());
        assertTrue(newUser.isAccountNonExpired());
        assertTrue(newUser.isCredentialsNonExpired());
        assertNotNull(newUser.getRoles());
        assertTrue(newUser.getRoles().isEmpty());
    }

    @Test
    void metadata_ShouldBeStoredAndRetrievedAsJson() {
        String metadata = generateMetadata("userProfile");
        user.setMetadata(metadata);
        assertEquals(metadata, user.getMetadata());
    }
}
