package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest extends BaseEntityTest {

    private Role role;
    private User user;
    private Participant participant;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("ROLE_" + generateUniqueCode());
        role.setDescription("Test Role Description");
        role.setPriority(1);
        role.setStatus(Role.STATUS_ACTIVE);
        role.setMetadata(generateMetadata("role"));
        role.setUsers(new HashSet<>());
        role.setParticipants(new HashSet<>());

        user = new User();
        user.setUsername("testuser_" + generateUniqueCode());
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setRoles(new HashSet<>());

        participant = new Participant();
        participant.setName("Test Participant");
        participant.setCode(generateUniqueCode());
        participant.setRoles(new HashSet<>());
    }

    @Test
    void isActive_WhenStatusIsActive_ShouldReturnTrue() {
        role.setStatus(Role.STATUS_ACTIVE);
        assertTrue(role.isActive());
    }

    @Test
    void isActive_WhenStatusIsNotActive_ShouldReturnFalse() {
        role.setStatus(Role.STATUS_INACTIVE);
        assertFalse(role.isActive());
    }

    @Test
    void hasParticipant_WhenParticipantExists_ShouldReturnTrue() {
        role.getParticipants().add(participant);
        assertTrue(role.hasParticipant(participant));
    }

    @Test
    void hasParticipant_WhenParticipantDoesNotExist_ShouldReturnFalse() {
        assertFalse(role.hasParticipant(participant));
    }

    @Test
    void hasParticipant_WhenParticipantsIsNull_ShouldReturnFalse() {
        role.setParticipants(null);
        assertFalse(role.hasParticipant(participant));
    }

    @Test
    void hasUser_WhenUserExists_ShouldReturnTrue() {
        role.getUsers().add(user);
        assertTrue(role.hasUser(user));
    }

    @Test
    void hasUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        assertFalse(role.hasUser(user));
    }

    @Test
    void hasUser_WhenUsersIsNull_ShouldReturnFalse() {
        role.setUsers(null);
        assertFalse(role.hasUser(user));
    }

    @Test
    void addParticipant_ShouldSetBidirectionalRelationship() {
        role.addParticipant(participant);
        
        assertTrue(role.getParticipants().contains(participant));
        assertTrue(participant.getRoles().contains(role));
    }

    @Test
    void addParticipant_WhenParticipantsIsNull_ShouldInitializeSet() {
        role.setParticipants(null);
        role.addParticipant(participant);
        
        assertNotNull(role.getParticipants());
        assertTrue(role.getParticipants().contains(participant));
    }

    @Test
    void addParticipant_WhenParticipantRolesIsNull_ShouldInitializeSet() {
        participant.setRoles(null);
        role.addParticipant(participant);
        
        assertNotNull(participant.getRoles());
        assertTrue(participant.getRoles().contains(role));
    }

    @Test
    void removeParticipant_ShouldRemoveBidirectionalRelationship() {
        role.addParticipant(participant);
        role.removeParticipant(participant);
        
        assertFalse(role.getParticipants().contains(participant));
        assertFalse(participant.getRoles().contains(role));
    }

    @Test
    void collections_ShouldBeInitializedByDefault() {
        Role newRole = new Role();
        assertNotNull(newRole.getUsers());
        assertNotNull(newRole.getParticipants());
        assertTrue(newRole.getUsers().isEmpty());
        assertTrue(newRole.getParticipants().isEmpty());
    }

    @Test
    void metadata_ShouldBeStoredAndRetrievedAsJson() {
        String metadata = generateMetadata("roleConfig");
        role.setMetadata(metadata);
        assertEquals(metadata, role.getMetadata());
    }
}
