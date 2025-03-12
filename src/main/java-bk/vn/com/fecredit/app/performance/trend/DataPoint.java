package vn.com.fecredit.app.performance.trend;

import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a single performance measurement at a point in time.
 * This is an immutable value class that ensures data consistency through validation.
 *
 * @param timestamp When the measurement was taken
 * @param value The measured performance value
 * @param deviation Standard deviation or error margin of the measurement
 */
public record DataPoint(
    @NonNull LocalDateTime timestamp,
    double value,
    double deviation
) implements Comparable<DataPoint> {

    /**
     * Creates a DataPoint with validation.
     * 
     * @throws IllegalArgumentException if timestamp is null or deviation is negative
     */
    public DataPoint {
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        if (deviation < 0) {
            throw new IllegalArgumentException("Deviation cannot be negative");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Value must be a finite number");
        }
        if (Double.isNaN(deviation) || Double.isInfinite(deviation)) {
            throw new IllegalArgumentException("Deviation must be a finite number");
        }
    }

    /**
     * Creates a DataPoint without deviation.
     *
     * @param timestamp When the measurement was taken
     * @param value The measured performance value
     * @return A new DataPoint with zero deviation
     */
    public static DataPoint of(@NonNull LocalDateTime timestamp, double value) {
        return new DataPoint(timestamp, value, 0.0);
    }

    /**
     * Creates a current DataPoint.
     *
     * @param value The measured performance value
     * @return A new DataPoint with current timestamp and zero deviation
     */
    public static DataPoint now(double value) {
        return new DataPoint(LocalDateTime.now(), value, 0.0);
    }

    /**
     * Calculates the time difference from another data point.
     *
     * @param other The other data point to compare with
     * @return Time difference in seconds
     */
    public long secondsFrom(@NonNull DataPoint other) {
        Objects.requireNonNull(other, "Other data point cannot be null");
        return Math.abs(ChronoUnit.SECONDS.between(this.timestamp, other.timestamp));
    }

    /**
     * Calculates the relative change from another value.
     *
     * @param otherValue The value to compare with
     * @return Relative change as a percentage (1.0 = 100%)
     */
    public double relativeChange(double otherValue) {
        if (otherValue == 0) {
            return value == 0 ? 0.0 : 1.0;
        }
        return (value - otherValue) / Math.abs(otherValue);
    }

    /**
     * Calculates the absolute change from another value.
     *
     * @param otherValue The value to compare with
     * @return Absolute difference between values
     */
    public double absoluteChange(double otherValue) {
        return value - otherValue;
    }

    /**
     * Checks if this measurement is considered stable.
     * A measurement is stable if its deviation is less than 10% of its value.
     *
     * @return true if the measurement is stable
     */
    public boolean isStable() {
        if (value == 0) return deviation == 0;
        return deviation / Math.abs(value) < 0.1;
    }

    @Override
    public int compareTo(DataPoint other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("DataPoint[time=%s, value=%.2f±%.2f]",
            timestamp, value, deviation);
    }

    /**
     * Creates a new DataPoint with updated value but same timestamp and deviation.
     *
     * @param newValue The new measurement value
     * @return A new DataPoint instance
     */
    public DataPoint withValue(double newValue) {
        return new DataPoint(timestamp, newValue, deviation);
    }

    /**
     * Creates a new DataPoint with updated deviation.
     *
     * @param newDeviation The new deviation value
     * @return A new DataPoint instance
     */
    public DataPoint withDeviation(double newDeviation) {
        return new DataPoint(timestamp, value, newDeviation);
    }
}
