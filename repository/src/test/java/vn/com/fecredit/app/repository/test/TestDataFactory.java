package vn.com.fecredit.app.repository.test;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.entity.*;

import java.time.LocalDateTime;

@Component
public class TestDataFactory {
    
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);
    
    public Region createRegion(String code, String name) {
        return Region.builder()
            .code(code)
            .name(name)
            .description("Test region " + name)
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .build();
    }

    public Event createEvent(String code, String name) {
        return Event.builder()
            .code(code)
            .name(name)
            .description("Test event " + name)
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now().plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .createdBy("testuser")
            .build();
    }

    public EventLocation createEventLocation(Event event, Region region, String code, String name) {
        EventLocation location = EventLocation.builder()
            .code(code)
            .name(name)
            .description("Test location " + name)
            .initialSpins(event.getInitialSpins())
            .dailySpinLimit(event.getDailySpinLimit())
            .defaultWinProbability(event.getDefaultWinProbability())
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .createdBy("testuser")
            .build();
        
        // Handle bidirectional relationships
        location.setEvent(event);
        location.setRegion(region);
        
        return location;
    }

    public Participant createParticipant(String code, String name) {
        return Participant.builder()
            .code(code)
            .name(name)
            .phone("0123456789")
            .email(name.toLowerCase().replace(" ", ".") + "@example.com")
            .account(code) // Using code as account since it's unique
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .createdBy("testuser")
            .build();
    }

    public Reward createReward(EventLocation location, String code, String name) {
        Reward reward = Reward.builder()
            .code(code)
            .name(name)
            .description("Test reward " + name)
            .points(100)
            .pointsRequired(50)
            .totalQuantity(10)
            .remainingQuantity(10)
            .dailyLimit(2)
            .winProbability(0.1)
            .validFrom(LocalDateTime.now())
            .validUntil(LocalDateTime.now().plusMonths(1))
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .createdBy("testuser")
            .build();
            
        reward.setEventLocation(location);
        return reward;
    }

    public ParticipantEvent createParticipantEvent(Participant participant, EventLocation location) {
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .totalSpins(location.getInitialSpins())
            .availableSpins(location.getInitialSpins())
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .createdBy("testuser")
            .build();
            
        participantEvent.setParticipant(participant);
        participantEvent.setEventLocation(location);
        return participantEvent;
    }

    public SpinHistory createSpinHistory(ParticipantEvent participantEvent, Reward reward, boolean win) {
        return SpinHistory.builder()
            .participant(participantEvent.getParticipant())
            .eventLocation(participantEvent.getEventLocation())
            .reward(reward)
            .timestamp(LocalDateTime.now())
            .win(win)
            .pointsEarned(win ? reward.getPoints() : 0)
            .pointsSpent(reward.getPointsRequired())
            .status(1)
            .version(0L)
            .createdAt(TEST_TIMESTAMP)
            .createdBy("testuser")
            .build();
    }
}
