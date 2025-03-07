package vn.com.fecredit.app.entity.base;

import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseEntityTest {
    
    protected LocalDateTime now;
    
    @BeforeEach
    void baseSetUp() {
        now = LocalDateTime.now();
    }
    
    protected String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    protected String generateMetadata(String type) {
        return String.format("{\"%sId\":\"%s\",\"timestamp\":\"%s\"}", 
            type, generateUniqueCode(), now);
    }
    
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    protected LocalDateTime futureDateTime(long plusDays) {
        return now.plusDays(plusDays);
    }
    
    protected LocalDateTime pastDateTime(long minusDays) {
        return now.minusDays(minusDays);
    }
    
    protected LocalDateTime futureMinutes(long plusMinutes) {
        return now.plusMinutes(plusMinutes);
    }
    
    protected LocalDateTime pastMinutes(long minusMinutes) {
        return now.minusMinutes(minusMinutes);
    }
    
    protected boolean isWithinRange(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
    
    protected boolean isWithinOneSecond(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return Math.abs(dateTime1.getSecond() - dateTime2.getSecond()) <= 1 &&
               dateTime1.withSecond(0).equals(dateTime2.withSecond(0));
    }
}
