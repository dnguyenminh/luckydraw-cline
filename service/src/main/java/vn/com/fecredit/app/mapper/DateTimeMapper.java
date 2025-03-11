package vn.com.fecredit.app.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.util.DateTimeUtils;

import java.time.LocalDateTime;

/**
 * Utility mapper class for handling date-time conversions
 */
@Component
public class DateTimeMapper {

    /**
     * Formats LocalDateTime to String using standard format
     *
     * @param dateTime The date time to format
     * @return Formatted date time string or null if input is null
     */
    @Named("formatDateTime")
    public String formatDateTime(LocalDateTime dateTime) {
        return DateTimeUtils.formatDateTime(dateTime);
    }

    /**
     * Parses String to LocalDateTime using standard format
     *
     * @param dateTimeStr The date time string to parse
     * @return Parsed LocalDateTime or null if input is null/invalid
     */
    @Named("parseDateTime")
    public LocalDateTime parseDateTime(String dateTimeStr) {
        return DateTimeUtils.parseDateTime(dateTimeStr);
    }

    /**
     * Formats LocalDateTime to date string using standard format
     *
     * @param dateTime The date time to format
     * @return Formatted date string or null if input is null
     */
    @Named("formatDate")
    public String formatDate(LocalDateTime dateTime) {
        return DateTimeUtils.formatDate(dateTime);
    }

    /**
     * Formats LocalDateTime to time string using standard format
     *
     * @param dateTime The date time to format
     * @return Formatted time string or null if input is null
     */
    @Named("formatTime")
    public String formatTime(LocalDateTime dateTime) {
        return DateTimeUtils.formatTime(dateTime);
    }
}
