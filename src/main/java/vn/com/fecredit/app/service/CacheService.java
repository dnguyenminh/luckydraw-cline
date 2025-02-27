package vn.com.fecredit.app.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface CacheService {

    /**
     * Put value in cache
     */
    <T> void put(String key, T value);

    /**
     * Put value in cache with expiration
     */
    <T> void put(String key, T value, Duration expiration);

    /**
     * Put value in cache with expiration in time unit
     */
    <T> void put(String key, T value, long timeout, TimeUnit unit);

    /**
     * Get value from cache
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Get value from cache or compute if absent
     */
    <T> T getOrCompute(String key, Class<T> type, java.util.function.Supplier<T> supplier);

    /**
     * Get value from cache or compute if absent with expiration
     */
    <T> T getOrCompute(String key, Class<T> type, java.util.function.Supplier<T> supplier, Duration expiration);

    /**
     * Remove value from cache
     */
    void remove(String key);

    /**
     * Remove values by pattern
     */
    void removeByPattern(String pattern);

    /**
     * Clear all cache
     */
    void clear();

    /**
     * Check if key exists
     */
    boolean exists(String key);

    /**
     * Get remaining time to live
     */
    Duration getTimeToLive(String key);

    /**
     * Set expiration
     */
    void setExpiration(String key, Duration expiration);

    /**
     * Extend expiration
     */
    void extendExpiration(String key, Duration extension);

    /**
     * Remove expiration
     */
    void removeExpiration(String key);

    /**
     * Put in hash
     */
    <T> void putInHash(String key, String field, T value);

    /**
     * Get from hash
     */
    <T> Optional<T> getFromHash(String key, String field, Class<T> type);

    /**
     * Get all hash entries
     */
    <T> Map<String, T> getHashEntries(String key, Class<T> type);

    /**
     * Remove from hash
     */
    void removeFromHash(String key, String field);

    /**
     * Add to set
     */
    <T> void addToSet(String key, T value);

    /**
     * Remove from set
     */
    <T> void removeFromSet(String key, T value);

    /**
     * Get set members
     */
    <T> Set<T> getSetMembers(String key, Class<T> type);

    /**
     * Add to list
     */
    <T> void addToList(String key, T value);

    /**
     * Get list elements
     */
    <T> List<T> getListElements(String key, Class<T> type);

    /**
     * Get list elements range
     */
    <T> List<T> getListElements(String key, Class<T> type, long start, long end);

    /**
     * Increment counter
     */
    long increment(String key);

    /**
     * Increment counter by value
     */
    long incrementBy(String key, long delta);

    /**
     * Decrement counter
     */
    long decrement(String key);

    /**
     * Get counter value
     */
    long getCounter(String key);

    /**
     * Reset counter
     */
    void resetCounter(String key);

    /**
     * Get cache statistics
     */
    CacheStats getCacheStats();

    /**
     * Cache statistics class
     */
    @lombok.Data
    class CacheStats {
        private final long hits;
        private final long misses;
        private final long evictions;
        private final int size;
        private final double hitRate;
        private final double missRate;
    }
}
