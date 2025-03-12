package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;

import java.time.LocalDateTime;
import java.util.HashSet;

public class TestEntitySetter {

    // Event setters
    public static void name(Event event, String name) {
        event.setName(name);
    }

    public static void setEventStatus(Event event, EntityStatus status) {
        event.setStatus(status);
    }

    // EventLocation setters
    public static void name(EventLocation location, String name) {
        location.setName(name);
    }

    public static void setLocationEvent(EventLocation location, Event event) {
        location.setEvent(event);
        if (event.getLocations() == null) {
            event.setLocations(new HashSet<>());
        }
        event.getLocations().add(location);
    }

    // Reward setters
    public static void name(Reward reward, String name) {
        reward.setName(name);
    }

    public static void setRewardEvent(Reward reward, Event event) {
        reward.setEvent(event);
        if (event.getRewards() == null) {
            event.setRewards(new HashSet<>());
        }
        event.getRewards().add(reward);
    }

    // Participant setters
    public static void customerId(Participant participant, String customerId) {
        participant.setCustomerId(customerId);
    }

    public static void setMetadata(SpinHistory spinHistory, String metadata) {
        spinHistory.setMetadata(metadata);
    }

    // SpinHistory setters
    public static void setSpinEvent(SpinHistory spinHistory, Event event) {
        spinHistory.setEvent(event);
        if (event.getSpinHistories() == null) {
            event.setSpinHistories(new HashSet<>());
        }
        event.getSpinHistories().add(spinHistory);
    }

    public static void setSpinLocation(SpinHistory spinHistory, EventLocation location) {
        spinHistory.setLocation(location);
        if (location.getSpinHistories() == null) {
            location.setSpinHistories(new HashSet<>());
        }
        location.getSpinHistories().add(spinHistory);
    }

    public static void setSpinReward(SpinHistory spinHistory, Reward reward) {
        spinHistory.setReward(reward);
        if (reward.getSpinHistories() == null) {
            reward.setSpinHistories(new HashSet<>());
        }
        reward.getSpinHistories().add(spinHistory);
    }

    public static void setSpinParticipant(SpinHistory spinHistory, Participant participant) {
        spinHistory.setParticipant(participant);
        if (participant.getSpinHistories() == null) {
            participant.setSpinHistories(new HashSet<>());
        }
        participant.getSpinHistories().add(spinHistory);
    }

    public static void spinTime(SpinHistory spinHistory, LocalDateTime time) {
        spinHistory.setSpinTime(time);
    }

    public static void setStatus(SpinHistory spinHistory, EntityStatus status) {
        spinHistory.setStatus(status);
    }
}
