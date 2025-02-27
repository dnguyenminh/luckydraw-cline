package vn.com.fecredit.app.util;

/**
 * Data class for generating test event scenarios
 */
public class EventScenario {
    private final String name;
    private final int totalSpins;
    private final int dailySpinLimit;
    private final int locationCount;
    private final int spinsPerLocation;
    private final double maxLocationMultiplier;
    private final int goldenHourCount;
    private final double maxGoldenHourMultiplier;
    private final int participantCount;

    private EventScenario(Builder builder) {
        this.name = builder.name;
        this.totalSpins = builder.totalSpins;
        this.dailySpinLimit = builder.dailySpinLimit;
        this.locationCount = builder.locationCount;
        this.spinsPerLocation = builder.spinsPerLocation;
        this.maxLocationMultiplier = builder.maxLocationMultiplier;
        this.goldenHourCount = builder.goldenHourCount;
        this.maxGoldenHourMultiplier = builder.maxGoldenHourMultiplier;
        this.participantCount = builder.participantCount;
    }

    public String getName() { return name; }
    public int getTotalSpins() { return totalSpins; }
    public int getDailySpinLimit() { return dailySpinLimit; }
    public int getLocationCount() { return locationCount; }
    public int getSpinsPerLocation() { return spinsPerLocation; }
    public double getMaxLocationMultiplier() { return maxLocationMultiplier; }
    public int getGoldenHourCount() { return goldenHourCount; }
    public double getMaxGoldenHourMultiplier() { return maxGoldenHourMultiplier; }
    public int getParticipantCount() { return participantCount; }

    public static class Builder {
        private String name;
        private int totalSpins = 1000;
        private int dailySpinLimit = 10;
        private int locationCount = 3;
        private int spinsPerLocation = 300;
        private double maxLocationMultiplier = 1.5;
        private int goldenHourCount = 2;
        private double maxGoldenHourMultiplier = 2.0;
        private int participantCount = 100;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder totalSpins(int totalSpins) {
            this.totalSpins = totalSpins;
            return this;
        }

        public Builder dailySpinLimit(int dailySpinLimit) {
            this.dailySpinLimit = dailySpinLimit;
            return this;
        }

        public Builder locationCount(int locationCount) {
            this.locationCount = locationCount;
            return this;
        }

        public Builder spinsPerLocation(int spinsPerLocation) {
            this.spinsPerLocation = spinsPerLocation;
            return this;
        }

        public Builder maxLocationMultiplier(double maxLocationMultiplier) {
            this.maxLocationMultiplier = maxLocationMultiplier;
            return this;
        }

        public Builder goldenHourCount(int goldenHourCount) {
            this.goldenHourCount = goldenHourCount;
            return this;
        }

        public Builder maxGoldenHourMultiplier(double maxGoldenHourMultiplier) {
            this.maxGoldenHourMultiplier = maxGoldenHourMultiplier;
            return this;
        }

        public Builder participantCount(int participantCount) {
            this.participantCount = participantCount;
            return this;
        }

        public EventScenario build() {
            if (name == null) {
                throw new IllegalStateException("Event name is required");
            }
            return new EventScenario(this);
        }
    }

    // Factory methods for common scenarios
    public static EventScenario highVolume() {
        return new Builder()
            .name("High Volume Event")
            .totalSpins(100000)
            .dailySpinLimit(100)
            .locationCount(5)
            .spinsPerLocation(20000)
            .maxLocationMultiplier(2.0)
            .goldenHourCount(6)
            .maxGoldenHourMultiplier(3.0)
            .participantCount(1000)
            .build();
    }

    public static EventScenario lowWinRate() {
        return new Builder()
            .name("Low Win Rate Event")
            .totalSpins(10000)
            .dailySpinLimit(20)
            .locationCount(3)
            .spinsPerLocation(3000)
            .maxLocationMultiplier(1.5)
            .goldenHourCount(2)
            .maxGoldenHourMultiplier(2.0)
            .participantCount(100)
            .build();
    }

    public static EventScenario smallScale() {
        return new Builder()
            .name("Small Scale Event")
            .totalSpins(1000)
            .dailySpinLimit(5)
            .locationCount(1)
            .spinsPerLocation(1000)
            .maxLocationMultiplier(1.2)
            .goldenHourCount(1)
            .maxGoldenHourMultiplier(1.5)
            .participantCount(50)
            .build();
    }

    public static EventScenario competitive() {
        return new Builder()
            .name("Competitive Event")
            .totalSpins(50000)
            .dailySpinLimit(50)
            .locationCount(10)
            .spinsPerLocation(5000)
            .maxLocationMultiplier(3.0)
            .goldenHourCount(8)
            .maxGoldenHourMultiplier(4.0)
            .participantCount(500)
            .build();
    }
}
