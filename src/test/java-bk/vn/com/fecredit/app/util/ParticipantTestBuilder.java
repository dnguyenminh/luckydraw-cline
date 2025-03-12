package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;

import java.time.LocalDateTime;
import java.util.HashSet;

public class ParticipantTestBuilder {

    public static Participant createTestParticipant() {
        return createTestParticipant(EntityStatus.ACTIVE);
    }

    public static Participant createTestParticipant(EntityStatus status) {
        Participant participant = new Participant();
        participant.setId(1L);
        participant.setStatus(status);
        participant.setSpinHistories(new HashSet<>());
        participant.setCreatedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());
        return participant;
    }

    public static Participant createTestParticipantWithEvent(Event event) {
        Participant participant = createTestParticipant();
        participant.setEvent(event);
        return participant;
    }

    public static Participant createParticipantWithSpinLimits(int dailyLimit, int totalLimit) {
        Participant participant = createTestParticipant();
        participant.setDailySpinLimit(dailyLimit);
        participant.setTotalSpinLimit(totalLimit);
        participant.setTotalSpins(0);
        return participant;
    }

    public static Participant createActiveParticipant() {
        Participant participant = createTestParticipant();
        participant.setStatus(EntityStatus.ACTIVE);
        participant.setDailySpinLimit(10);
        participant.setTotalSpinLimit(100);
        participant.setTotalSpins(0);
        participant.setLastSpinDate(LocalDateTime.now().minusDays(1));
        return participant;
    }

    public static Participant createInactiveParticipant() {
        Participant participant = createTestParticipant();
        participant.setStatus(EntityStatus.INACTIVE);
        participant.setDailySpinLimit(0);
        participant.setTotalSpinLimit(0);
        participant.setTotalSpins(0);
        return participant;
    }

    public static Participant createExpiredParticipant() {
        Participant participant = createTestParticipant();
        participant.setStatus(EntityStatus.ACTIVE);
        participant.setDailySpinLimit(10);
        participant.setTotalSpinLimit(100);
        participant.setTotalSpins(100);
        participant.setLastSpinDate(LocalDateTime.now().minusDays(30));
        return participant;
    }
}
