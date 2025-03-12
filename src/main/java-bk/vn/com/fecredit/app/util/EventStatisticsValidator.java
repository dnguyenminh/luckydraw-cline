package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Validator for EventStatistics inputs and data
 */
public class EventStatisticsValidator {
    
    public static void validateEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
    }

    public static void validateLocation(EventLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (location.getWinProbabilityMultiplier() < 0) {
            throw new IllegalStateException("Probability multiplier must be positive");
        }
    }

    public static void validateSpinHistories(List<SpinHistory> spinHistories) {
        if (spinHistories == null) {
            return; // Return empty results rather than throwing exception
        }
        
        spinHistories.forEach(spin -> {
            if (spin.getSpinTime() == null) {
                throw new IllegalStateException("Spin time cannot be null");
            }
            if (spin.isWin() && spin.getReward() == null) {
                throw new IllegalStateException("Winning spin must have associated reward");
            }
        });
    }

    public static void validateTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    public static void validateLocationMultiplier(double multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Location multiplier must be positive");
        }
        if (Double.isInfinite(multiplier) || Double.isNaN(multiplier)) {
            throw new IllegalArgumentException("Invalid location multiplier value");
        }
    }

    public static void validateWinProbability(double probability) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Win probability must be between 0 and 1");
        }
        if (Double.isNaN(probability)) {
            throw new IllegalArgumentException("Win probability cannot be NaN");
        }
    }

    public static void validateReward(Reward reward) {
        if (reward == null) {
            throw new IllegalArgumentException("Reward cannot be null");
        }
        if (reward.getWinProbability() < 0 || reward.getWinProbability() > 1) {
            throw new IllegalArgumentException("Invalid reward win probability");
        }
    }

    public static void validateSpinHistory(SpinHistory spin) {
        if (spin == null) {
            throw new IllegalArgumentException("Spin history cannot be null");
        }
        if (spin.getSpinTime() == null) {
            throw new IllegalArgumentException("Spin time cannot be null");
        }
        if (spin.getEvent() == null) {
            throw new IllegalArgumentException("Spin must be associated with an event");
        }
        if (spin.isWin() && spin.getReward() == null) {
            throw new IllegalStateException("Winning spin must have a reward");
        }
    }

    public static void validateGoldenHour(GoldenHour goldenHour) {
        if (goldenHour == null) {
            throw new IllegalArgumentException("Golden hour cannot be null");
        }
        if (goldenHour.getStartTime() == null || goldenHour.getEndTime() == null) {
            throw new IllegalArgumentException("Golden hour must have start and end times");
        }
        if (goldenHour.getStartTime().isAfter(goldenHour.getEndTime())) {
            throw new IllegalArgumentException("Golden hour start time must be before end time");
        }
        if (goldenHour.getProbabilityMultiplier() <= 0) {
            throw new IllegalArgumentException("Golden hour multiplier must be positive");
        }
    }

    public static void validateAnalysisInputs(Event event, List<SpinHistory> spins) {
        validateEvent(event);
        if (spins != null) {
            spins.forEach(spin -> {
                if (spin != null && !Objects.equals(spin.getEvent().getId(), event.getId())) {
                    throw new IllegalArgumentException("Spin history must belong to the analyzed event");
                }
            });
        }
    }
}
