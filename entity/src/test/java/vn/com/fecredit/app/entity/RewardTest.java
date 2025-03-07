package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

class RewardTest extends BaseEntityTest {

    private Reward reward;
    private Event event;
    private EventLocation eventLocation;
    private Region region;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test entities ===");
        testTime = LocalDateTime.now();
        
        // Set up region first
        region = Region.builder()
            .name("Test Region")
            .code(generateUniqueCode())
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(testTime)
            .updatedAt(testTime)
            .createdBy("test")
            .updatedBy("test")
            .provinces(new HashSet<>())
            .eventLocations(new HashSet<>())
            .build();

        verifyEntityState(region, "Region");

        // Set up event with valid time range
        event = Event.builder()
            .name("Test Event")
            .code(generateUniqueCode())
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .startTime(pastMinutes(60))
            .endTime(futureMinutes(60))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(testTime)
            .updatedAt(testTime)
            .createdBy("test")
            .updatedBy("test")
            .eventLocations(new LinkedHashSet<>())
            .rewards(new LinkedHashSet<>())
            .goldenHours(new LinkedHashSet<>())
            .spinHistories(new LinkedHashSet<>())
            .build();

        verifyEntityState(event, "Event");

        // Set up event location
        eventLocation = EventLocation.builder()
            .name("Test Location")
            .code(generateUniqueCode())
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(testTime)
            .updatedAt(testTime)
            .createdBy("test")
            .updatedBy("test")
            .participantEvents(new HashSet<>())
            .rewards(new HashSet<>())
            .goldenHours(new HashSet<>())
            .spinHistories(new HashSet<>())
            .build();

        // Set up relationships
        eventLocation.setRegion(region);
        event.addLocation(eventLocation);

        verifyEntityState(eventLocation, "EventLocation");

        // Set up reward
        reward = Reward.builder()
            .name("Test Reward")
            .code(generateUniqueCode())
            .description("Test Description")
            .dailyLimit(10)
            .totalQuantity(100)
            .remainingQuantity(100)
            .pointsRequired(50)
            .winProbability(0.1)
            .validFrom(pastMinutes(30))
            .validUntil(futureMinutes(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .dailyCount(0)
            .createdAt(testTime)
            .updatedAt(testTime)
            .createdBy("test")
            .updatedBy("test")
            .build();

        reward.setEventLocation(eventLocation);
        verifyEntityState(reward, "Reward");
    }

    private void verifyEntityState(AbstractStatusAwareEntity entity, String entityName) {
        System.out.println("\n=== Verifying " + entityName + " state ===");
        System.out.println("Status value: " + entity.getStatus());
        System.out.println("Status name: " + entity.getStatusName());
        System.out.println("Created at: " + entity.getCreatedAt());
        System.out.println("Updated at: " + entity.getUpdatedAt());
        System.out.println("Created by: " + entity.getCreatedBy());
        System.out.println("Updated by: " + entity.getUpdatedBy());
        System.out.println("Active check: " + entity.isActive());

        assertEquals(AbstractStatusAwareEntity.STATUS_ACTIVE, entity.getStatus(), entityName + " status should be active");
        assertTrue(entity.isActive(), entityName + " should be active");
        assertNotNull(entity.getCreatedAt(), entityName + " created at should be set");
        assertNotNull(entity.getUpdatedAt(), entityName + " updated at should be set");
        assertNotNull(entity.getCreatedBy(), entityName + " created by should be set");
        assertNotNull(entity.getUpdatedBy(), entityName + " updated by should be set");
    }

    @Test
    void isActive_ShouldConsiderDailyLimit() {
        System.out.println("\n=== Testing daily limit ===");
        
        // Verify preconditions
        verifyEntityState(region, "Region");
        verifyEntityState(event, "Event");
        verifyEntityState(eventLocation, "EventLocation");
        verifyEntityState(reward, "Reward");
        
        assertTrue(event.isActive(), "Event should be active");
        assertTrue(eventLocation.isActive(), "Event location should be active");
        
        // Set up test conditions
        reward.setRemainingQuantity(1);
        reward.setDailyLimit(2);
        reward.setDailyCount(0);
        reward.setValidFrom(pastMinutes(30));
        reward.setValidUntil(futureMinutes(30));

        System.out.println("\nTest conditions:");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Current time: " + now);
        System.out.println("Reward valid from: " + reward.getValidFrom());
        System.out.println("Reward valid until: " + reward.getValidUntil());
        System.out.println("Time valid check: " + (now.compareTo(reward.getValidFrom()) >= 0 && now.compareTo(reward.getValidUntil()) <= 0));
        System.out.println("Daily limit: " + reward.getDailyLimit());
        System.out.println("Daily count: " + reward.getDailyCount());

        assertTrue(reward.isActive(), "Should be active when under daily limit");
        
        reward.setDailyCount(2);
        assertFalse(reward.isActive(), "Should be inactive when daily limit is reached");

        reward.resetDailyLimit();
        assertTrue(reward.isActive(), "Should be active again after daily count reset");
    }
}
