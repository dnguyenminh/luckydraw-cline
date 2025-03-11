package vn.com.fecredit.app.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.RandomStringGenerator;

import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {

    private static final RandomStringGenerator CODE_GENERATOR = new RandomStringGenerator.Builder()
            .withinRange('0', 'Z')
            .filteredBy(Character::isLetterOrDigit)
            .build();

    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

    /**
     * Generate random code with specified length
     */
    public static String generateCode(int length) {
        return CODE_GENERATOR.generate(length);
    }

    /**
     * Generate UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate short UUID (first 8 characters)
     */
    public static String generateShortUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Remove diacritics (accent marks) from a string
     */
    public static String removeDiacritics(String str) {
        if (str == null) {
            return null;
        }
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return DIACRITICS_AND_FRIENDS.matcher(normalized).replaceAll("");
    }

    /**
     * Convert string to slug format
     */
    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }
        // Remove diacritics
        String normalized = removeDiacritics(input.toLowerCase());
        
        // Replace spaces with hyphens
        normalized = normalized.replaceAll("\\s+", "-");
        
        // Remove all non-word chars except hyphens
        normalized = normalized.replaceAll("[^a-zA-Z0-9-]", "");
        
        // Remove multiple consecutive hyphens
        normalized = normalized.replaceAll("-+", "-");
        
        // Remove leading/trailing hyphens
        return normalized.replaceAll("^-|-$", "");
    }

    /**
     * Truncate string to max length with ellipsis
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Mask sensitive data
     */
    public static String maskSensitiveData(String data, int visibleCharsStart, int visibleCharsEnd) {
        if (data == null) {
            return null;
        }
        int length = data.length();
        if (length <= (visibleCharsStart + visibleCharsEnd)) {
            return "*".repeat(length);
        }
        return data.substring(0, visibleCharsStart) +
               "*".repeat(length - visibleCharsStart - visibleCharsEnd) +
               data.substring(length - visibleCharsEnd);
    }

    /**
     * Format phone number
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        // Remove all non-digit characters
        String digitsOnly = phone.replaceAll("\\D", "");
        
        // Format based on length
        if (digitsOnly.length() == 10) {
            return String.format("%s.%s.%s",
                    digitsOnly.substring(0, 3),
                    digitsOnly.substring(3, 6),
                    digitsOnly.substring(6));
        }
        return digitsOnly;
    }

    /**
     * Normalize string for search
     */
    public static String normalizeForSearch(String input) {
        if (input == null) {
            return null;
        }
        // Remove diacritics and convert to lowercase
        String normalized = removeDiacritics(input.toLowerCase());
        
        // Remove all special characters and extra spaces
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }

    /**
     * Extract numbers from string
     */
    public static String extractNumbers(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\D", "");
    }

    /**
     * Check if string contains only digits
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Get default if string is null or empty
     */
    public static String getDefault(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
}
