package vn.com.fecredit.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Component
@ConfigurationProperties(prefix = "lucky-draw")
public class LuckyDrawProperties {

    private final GoldenHour goldenHour = new GoldenHour();
    private final Spin spin = new Spin();
    private final Reward reward = new Reward();
    private final Event event = new Event();

    @Data
    public static class GoldenHour {
        private Double minDuration;
        private Integer maxDuration;
        private Double maxMultiplier;

        public boolean isValidDuration(double hours) {
            return hours >= minDuration && hours <= maxDuration;
        }

        public boolean isValidDurationBetween(LocalDateTime start, LocalDateTime end) {
            double hours = ChronoUnit.MINUTES.between(start, end) / 60.0;
            return isValidDuration(hours);
        }

        public boolean isValidMultiplier(double multiplier) {
            return multiplier >= 1.0 && multiplier <= maxMultiplier;
        }

        public Double normalizeMultiplier(Double multiplier) {
            if (multiplier == null) return 1.0;
            return Math.min(Math.max(multiplier, 1.0), maxMultiplier);
        }
    }

    @Data
    public static class Spin {
        private Integer minCooldown;
        private Integer defaultDailyLimit;
        private Integer maxDailyLimit;

        public boolean isValidDailyLimit(int limit) {
            return limit > 0 && limit <= maxDailyLimit;
        }

        public boolean canSpin(LocalDateTime lastSpinTime) {
            if (lastSpinTime == null) {
                return true;
            }
            return LocalDateTime.now()
                .isAfter(lastSpinTime.plusSeconds(minCooldown));
        }

        public LocalDateTime getNextAvailableSpinTime(LocalDateTime lastSpinTime) {
            if (lastSpinTime == null) {
                return LocalDateTime.now();
            }
            return lastSpinTime.plusSeconds(minCooldown);
        }

        public Integer normalizeDailyLimit(Integer limit) {
            if (limit == null) return defaultDailyLimit;
            return Math.min(Math.max(limit, 1), maxDailyLimit);
        }
    }

    @Data
    public static class Reward {
        private Double minProbability;
        private Double maxProbability;
        private String codePattern;

        public boolean isValidProbability(double probability) {
            return probability >= minProbability && probability <= maxProbability;
        }

        public boolean isValidCode(String code) {
            return code != null && code.matches(codePattern);
        }

        public Double normalizeProbability(Double probability) {
            if (probability == null) return minProbability;
            return Math.min(Math.max(probability, minProbability), maxProbability);
        }

        public String normalizeCode(String code) {
            if (code == null) return null;
            return code.toUpperCase().trim();
        }
    }

    @Data
    public static class Event {
        private Integer minDuration;
        private Integer maxDuration;
        private String codePattern;

        public boolean isValidDuration(int days) {
            return days >= minDuration && days <= maxDuration;
        }

        public boolean isValidDurationBetween(LocalDateTime start, LocalDateTime end) {
            if (start == null || end == null) return false;
            long days = ChronoUnit.DAYS.between(start, end);
            return isValidDuration((int) days);
        }

        public boolean isValidCode(String code) {
            return code != null && code.matches(codePattern);
        }

        public String normalizeCode(String code) {
            if (code == null) return null;
            return code.toUpperCase().trim();
        }

        public boolean isWithinValidPeriod(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime checkDate) {
            if (startDate == null || endDate == null || checkDate == null) return false;
            return !checkDate.isBefore(startDate) && !checkDate.isAfter(endDate);
        }
    }
}
