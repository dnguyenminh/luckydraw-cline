package vn.com.fecredit.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.Participant;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

public class TestUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON string", e);
        }
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON string to object", e);
        }
    }

    /**
     * Creates a test participant with basic role
     */
    public static Participant createTestParticipant() {
        User user = createTestParticipantUser();
        return createParticipant(user, false);
    }

    /**
     * Creates a test premium participant
     */
    public static Participant createTestPremiumParticipant() {
        User user = createTestPremiumParticipantUser();
        return createParticipant(user, true);
    }

    /**
     * Creates a test participant user account
     */
    public static User createTestParticipantUser() {
        Role role = TestRoleConstants.createParticipantRole();
        return createUserWithRole(role, 1L, "participant", "participant@example.com");
    }

    /**
     * Creates a test premium participant user account
     */
    public static User createTestPremiumParticipantUser() {
        Role role = TestRoleConstants.createPremiumParticipantRole();
        return createUserWithRole(role, 2L, "premium", "premium@example.com");
    }

    /**
     * Creates a test staff user account
     */
    public static User createTestStaffUser() {
        Role role = TestRoleConstants.createUserRole();
        return createUserWithRole(role, 3L, "staff", "staff@example.com");
    }

    /**
     * Creates a test admin user account
     */
    public static User createTestAdminUser() {
        Role role = TestRoleConstants.createAdminRole();
        return createUserWithRole(role, 4L, "admin", "admin@example.com");
    }

    private static User createUserWithRole(Role role, Long userId, String username, String email) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName(role.getName().name().replace("ROLE_", ""));
        user.setStatus(EntityStatus.ACTIVE);
        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        return user;
    }

    private static Participant createParticipant(User user, boolean isPremium) {
        Participant participant = new Participant();
        participant.setId(user.getId());
        participant.setUser(user);
        participant.setPremium(isPremium);
        participant.setRegistrationDate(LocalDateTime.now());
        participant.setLastLoginDate(LocalDateTime.now());
        participant.setTotalSpins(0);
        participant.setDailySpinsLeft(isPremium ? 5 : 3);
        participant.setTotalRewards(0);
        participant.setStatus(EntityStatus.ACTIVE);
        return participant;
    }

    /**
     * Deep copies an object by serializing to JSON and back
     */
    public static <T> T deepCopy(T object, Class<T> clazz) {
        return fromJsonString(asJsonString(object), clazz);
    }
}
