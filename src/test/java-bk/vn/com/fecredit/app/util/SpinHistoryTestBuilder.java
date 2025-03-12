package vn.com.fecredit.app.util;

import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpinHistoryTestBuilder {

    public static SpinHistory createTestSpinHistory() {
        return createTestSpinHistory(EntityStatus.ACTIVE);
    }

    public static SpinHistory createTestSpinHistory(EntityStatus status) {
        SpinHistory spin = new SpinHistory();
        spin.setSpinTime(LocalDateTime.now());
        spin.setWinning(false);
        spin.setWinProbability(0.1);
        spin.setProbabilityMultiplier(1.0);
        spin.setFinalProbability(0.1);
        spin.setSpinResult("NO_WIN");
        spin.setLatitude(10.762622);
        spin.setLongitude(106.660172);
        spin.setDeviceId("TEST-DEVICE-" + UUID.randomUUID().toString().substring(0, 8));
        spin.setSessionId(UUID.randomUUID().toString());
        spin.setGoldenHourActive(false);
        spin.setGoldenHourMultiplier(1.0);
        spin.setNotes("Test spin");
        spin.setMetadata("{}");
        spin.setStatus(status);
        return spin;
    }

    public static SpinHistory createWinningSpinHistory(Reward reward) {
        SpinHistory spin = createTestSpinHistory();
        spin.setWinning(true);
        spin.setReward(reward);
        spin.setSpinResult("WIN_" + (reward != null ? reward.getCode() : "UNKNOWN"));
        spin.setFinalProbability(0.5);
        return spin;
    }

    public static SpinHistory createGoldenHourSpinHistory() {
        SpinHistory spin = createTestSpinHistory();
        spin.setGoldenHourActive(true);
        spin.setGoldenHourMultiplier(2.0);
        spin.setWinProbability(0.1);
        spin.setProbabilityMultiplier(1.0);
        spin.setFinalProbability(0.2);
        return spin;
    }

    public static SpinHistory createCompleteSpinHistory(Event event, Participant participant, 
                                                      EventLocation location, Reward reward) {
        SpinHistory spin = createTestSpinHistory();
        spin.setEvent(event);
        spin.setParticipant(participant);
        spin.setEventLocation(location);
        spin.setReward(reward);
        return spin;
    }

    public static SpinHistoryDTO.CreateRequest createTestCreateRequest() {
        return SpinHistoryDTO.CreateRequest.builder()
                .eventId(1L)
                .participantId(1L)
                .locationId(1L)
                .latitude(10.762622)
                .longitude(106.660172)
                .deviceId("TEST-DEVICE-" + UUID.randomUUID().toString().substring(0, 8))
                .sessionId(UUID.randomUUID().toString())
                .metadata("{}")
                .build();
    }

    public static SpinHistoryDTO.UpdateResult createTestUpdateResult(boolean winning, Long rewardId) {
        return SpinHistoryDTO.UpdateResult.builder()
                .id(1L)
                .winning(winning)
                .rewardId(rewardId)
                .spinResult(winning ? "WIN_REWARD_" + rewardId : "NO_WIN")
                .finalProbability(winning ? 0.5 : 0.1)
                .notes("Test update")
                .build();
    }

    public static SpinHistory createSpinHistoryWithProbability(double winProbability, 
                                                             double multiplier, 
                                                             boolean goldenHourActive,
                                                             double goldenHourMultiplier) {
        SpinHistory spin = createTestSpinHistory();
        spin.setWinProbability(winProbability);
        spin.setProbabilityMultiplier(multiplier);
        spin.setGoldenHourActive(goldenHourActive);
        spin.setGoldenHourMultiplier(goldenHourMultiplier);
        
        double finalProb = winProbability * multiplier;
        if (goldenHourActive) {
            finalProb *= goldenHourMultiplier;
        }
        spin.setFinalProbability(finalProb);
        
        return spin;
    }

    public static SpinHistory createExpiredSpin() {
        SpinHistory spin = createTestSpinHistory(EntityStatus.INACTIVE);
        spin.setSpinTime(LocalDateTime.now().minusDays(30));
        return spin;
    }

    public static SpinHistory createInvalidSpin() {
        return new SpinHistory(); // Creates spin with no required fields set
    }
}
