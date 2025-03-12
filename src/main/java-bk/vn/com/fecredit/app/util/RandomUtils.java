package vn.com.fecredit.app.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@UtilityClass
public class RandomUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Generate random integer between min and max (inclusive)
     */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generate random long between min and max (inclusive)
     */
    public static long randomLong(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    /**
     * Generate random double between min and max
     */
    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generate random boolean with specified probability
     */
    public static boolean randomBoolean(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    /**
     * Select random element from list
     */
    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Select random elements from list
     */
    public static <T> List<T> randomElements(List<T> list, int count) {
        if (list == null || list.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(list)
            .stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                collected -> {
                    Collections.shuffle(collected);
                    return collected.stream()
                        .limit(count)
                        .collect(Collectors.toList());
                }
            ));
    }

    /**
     * Generate random string of specified length
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Generate random numeric string of specified length
     */
    public static String randomNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Shuffle list randomly
     */
    public static <T> List<T> shuffle(List<T> list) {
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled, SECURE_RANDOM);
        return shuffled;
    }

    /**
     * Select random item based on weights
     */
    public static <T> T weightedRandom(List<T> items, List<Double> weights) {
        if (items == null || weights == null || items.size() != weights.size() || items.isEmpty()) {
            return null;
        }

        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        double random = ThreadLocalRandom.current().nextDouble() * totalWeight;
        
        double cumulativeWeight = 0;
        for (int i = 0; i < items.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (random < cumulativeWeight) {
                return items.get(i);
            }
        }
        
        return items.get(items.size() - 1);
    }

    /**
     * Generate random date between two dates
     */
    public static java.time.LocalDateTime randomDateTime(
            java.time.LocalDateTime startInclusive, 
            java.time.LocalDateTime endInclusive) {
        long startEpochDay = startInclusive.toLocalDate().toEpochDay();
        long endEpochDay = endInclusive.toLocalDate().toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        
        java.time.LocalDate randomDate = java.time.LocalDate.ofEpochDay(randomDay);
        int randomHour = ThreadLocalRandom.current().nextInt(24);
        int randomMinute = ThreadLocalRandom.current().nextInt(60);
        int randomSecond = ThreadLocalRandom.current().nextInt(60);
        
        return java.time.LocalDateTime.of(
            randomDate.getYear(),
            randomDate.getMonth(),
            randomDate.getDayOfMonth(),
            randomHour,
            randomMinute,
            randomSecond
        );
    }

    /**
     * Generate secure random bytes
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
}
