package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.dto.RoleDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.*;
import java.time.*;

/**
 * Utility class for creating test data and common test operations
 */
public class TestUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Direct entity creation methods
    public static User createTestUser() {
        return TestDataBuilder.aUser().build();
    }
    
    public static User createTestUserWithRole(RoleName roleName) {
        User user = TestDataBuilder.aUser().build();
        Role role = TestDataBuilder.aRole()
            .name(roleName)
            .build();
        user.getRoles().add(role);
        role.getUsers().add(user);
        return user;
    }

    public static Event createActiveEvent() {
        return TestDataBuilder.anEvent()
            .isActive(true)
            .deleted(false)
            .build();
    }
    
    public static Event createEventWithReward() {
        Event event = TestDataBuilder.anEventInProgress();
        Reward reward = TestDataBuilder.aReward().build();
        event.addReward(reward);
        reward.setEvent(event);
        return event;
    }

    public static Event createEventWithGoldenHour() {
        Event event = TestDataBuilder.anEventInProgress();
        GoldenHour goldenHour = TestDataBuilder.aGoldenHour().build();
        event.addGoldenHour(goldenHour);
        goldenHour.setEvent(event);
        return event;
    }

    public static Event createComplexEvent() {
        Event event = TestDataBuilder.anEventInProgress();
        
        // Add locations
        for (int i = 0; i < 3; i++) {
            EventLocation location = TestDataBuilder.anEventLocation().build();
            event.addLocation(location);
            location.setEvent(event);
        }
        
        // Add rewards
        for (int i = 0; i < 3; i++) {
            Reward reward = TestDataBuilder.aReward().build();
            event.addReward(reward);
            reward.setEvent(event);
        }
        
        // Add golden hours
        for (int i = 0; i < 2; i++) {
            GoldenHour goldenHour = TestDataBuilder.aGoldenHour().build();
            event.addGoldenHour(goldenHour);
            goldenHour.setEvent(event);
        }
        
        // Add participants
        for (int i = 0; i < 5; i++) {
            Participant participant = TestDataBuilder.aParticipant().build();
            event.addParticipant(participant);
            participant.setEvent(event);
        }
        
        return event;
    }

    // JSON conversion methods for integration tests
    public static String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }
    
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }

    // Collection utility methods
    public static <T> List<T> toList(T... items) {
        return Arrays.asList(items);
    }
    
    public static <T> Set<T> toSet(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    // Validation utility methods
    public static void validateComplexEvent(Event event) {
        Objects.requireNonNull(event, "Event cannot be null");
        Objects.requireNonNull(event.getLocations(), "Event locations cannot be null");
        Objects.requireNonNull(event.getRewards(), "Event rewards cannot be null");
        Objects.requireNonNull(event.getGoldenHours(), "Event golden hours cannot be null");
        Objects.requireNonNull(event.getParticipants(), "Event participants cannot be null");
        
        if (event.getLocations().isEmpty()) {
            throw new IllegalStateException("Event must have at least one location");
        }
        if (event.getRewards().isEmpty()) {
            throw new IllegalStateException("Event must have at least one reward");
        }
    }

    public static void validateEventTiming(Event event) {
        Objects.requireNonNull(event.getStartDate(), "Event start date cannot be null");
        Objects.requireNonNull(event.getEndDate(), "Event end date cannot be null");
        
        if (event.getStartDate().isAfter(event.getEndDate())) {
            throw new IllegalStateException("Event start date must be before end date");
        }
    }

    public static void validateParticipant(Participant participant) {
        Objects.requireNonNull(participant, "Participant cannot be null");
        Objects.requireNonNull(participant.getCustomerId(), "Customer ID cannot be null");
        Objects.requireNonNull(participant.getCardNumber(), "Card number cannot be null");
        Objects.requireNonNull(participant.getPhoneNumber(), "Phone number cannot be null");
        Objects.requireNonNull(participant.getEmail(), "Email cannot be null");
    }

    public static void validateSpinHistory(SpinHistory spinHistory) {
        Objects.requireNonNull(spinHistory, "Spin history cannot be null");
        Objects.requireNonNull(spinHistory.getSpinTime(), "Spin time cannot be null");
        Objects.requireNonNull(spinHistory.getParticipant(), "Participant cannot be null");
        Objects.requireNonNull(spinHistory.getEvent(), "Event cannot be null");
    }

    public static void resetTestData() {
        TestDataBuilder.resetCounter();
    }
}
