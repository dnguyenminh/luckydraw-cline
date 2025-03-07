package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BlacklistedTokenTest extends BaseEntityTest {

    private BlacklistedToken blacklistedToken;
    private User user;

    @BeforeEach
    void setUp() {
        blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(generateUniqueCode() + ".JWT." + generateUniqueCode());
        blacklistedToken.setTokenType("Bearer");
        blacklistedToken.setStatus(BlacklistedToken.STATUS_ACTIVE);
        blacklistedToken.setExpiryDate(futureMinutes(30));
        blacklistedToken.setMetadata(generateMetadata("token"));

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    void isExpired_WhenCurrentTimeIsAfterExpiryDate_ShouldReturnTrue() {
        blacklistedToken.setExpiryDate(pastMinutes(1));
        assertTrue(blacklistedToken.isExpired());
    }

    @Test
    void isExpired_WhenCurrentTimeIsBeforeExpiryDate_ShouldReturnFalse() {
        blacklistedToken.setExpiryDate(futureMinutes(1));
        assertFalse(blacklistedToken.isExpired());
    }

    @Test
    void isActive_WhenStatusActiveAndNotExpired_ShouldReturnTrue() {
        blacklistedToken.setStatus(BlacklistedToken.STATUS_ACTIVE);
        blacklistedToken.setExpiryDate(futureMinutes(30));
        
        assertTrue(blacklistedToken.isActive());
    }

    @Test
    void isActive_WhenStatusActiveButExpired_ShouldReturnFalse() {
        blacklistedToken.setStatus(BlacklistedToken.STATUS_ACTIVE);
        blacklistedToken.setExpiryDate(pastMinutes(1));
        
        assertFalse(blacklistedToken.isActive());
    }

    @Test
    void isActive_WhenStatusNotActive_ShouldReturnFalse() {
        blacklistedToken.setStatus(BlacklistedToken.STATUS_EXPIRED);
        blacklistedToken.setExpiryDate(futureMinutes(30));
        
        assertFalse(blacklistedToken.isActive());
    }

    @Test
    void markAsExpired_ShouldUpdateStatus() {
        blacklistedToken.markAsExpired();
        assertEquals(BlacklistedToken.STATUS_EXPIRED, blacklistedToken.getStatus());
    }

    @Test
    void markAsRevoked_ShouldUpdateStatusAndReason() {
        String reason = "Security breach detected at " + now;
        blacklistedToken.markAsRevoked(reason);
        
        assertEquals(BlacklistedToken.STATUS_REVOKED, blacklistedToken.getStatus());
        assertEquals(reason, blacklistedToken.getReason());
    }

    @Test
    void userRelationship_ShouldBeProperlyManaged() {
        blacklistedToken.setUser(user);
        assertEquals(user, blacklistedToken.getUser());
    }

    @Test
    void metadata_ShouldBeStoredAndRetrievedAsJson() {
        String metadata = generateMetadata("blacklist");
        blacklistedToken.setMetadata(metadata);
        assertEquals(metadata, blacklistedToken.getMetadata());
    }

    @Test
    void expiryDateComparison_ShouldBeAccurate() {
        LocalDateTime expiry = futureMinutes(1);
        blacklistedToken.setExpiryDate(expiry);
        
        assertTrue(isWithinOneSecond(expiry, blacklistedToken.getExpiryDate()));
        assertFalse(blacklistedToken.isExpired());
        
        sleep(1100); // Wait just over 1 second
        if (now.isAfter(expiry)) {
            assertTrue(blacklistedToken.isExpired());
        }
    }
}
