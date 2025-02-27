package vn.com.fecredit.app.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SpinResultDTOTest {

    @Test
    void shouldBuildWithDefaultValues() {
        SpinResultDTO result = SpinResultDTO.builder().build();
        
        assertFalse(result.getWon());
        assertNull(result.getRewardId());
        assertNull(result.getRewardName());
        assertFalse(result.getIsGoldenHour());
        assertEquals(1.0, result.getMultiplier());
        assertEquals(0L, result.getRemainingSpins());
        assertNull(result.getEventId());
        assertNull(result.getLocation());
    }

    @Test
    void shouldBuildWithAllValues() {
        SpinResultDTO result = SpinResultDTO.builder()
                .won(true)
                .rewardId(1L)
                .rewardName("Test Reward")
                .isGoldenHour(true)
                .multiplier(2.0)
                .remainingSpins(5L)
                .eventId(1L)
                .location("TEST")
                .build();
        
        assertTrue(result.getWon());
        assertEquals(1L, result.getRewardId());
        assertEquals("Test Reward", result.getRewardName());
        assertTrue(result.getIsGoldenHour());
        assertEquals(2.0, result.getMultiplier());
        assertEquals(5L, result.getRemainingSpins());
        assertEquals(1L, result.getEventId());
        assertEquals("TEST", result.getLocation());
    }

    @Test
    void shouldHandleNullValues() {
        SpinResultDTO result = SpinResultDTO.builder()
                .won(null)
                .rewardId(null)
                .rewardName(null)
                .isGoldenHour(null)
                .multiplier(null)
                .remainingSpins((Long) null)
                .eventId(null)
                .location(null)
                .build();
        
        assertFalse(result.getWon());
        assertNull(result.getRewardId());
        assertNull(result.getRewardName());
        assertFalse(result.getIsGoldenHour());
        assertEquals(1.0, result.getMultiplier());
        assertEquals(0L, result.getRemainingSpins());
        assertNull(result.getEventId());
        assertNull(result.getLocation());
    }

    @Test
    void shouldHandleIntegerRemainingSpins() {
        SpinResultDTO result = SpinResultDTO.builder()
                .remainingSpins(5)
                .build();
        
        assertEquals(5L, result.getRemainingSpins());
    }

    @Test
    void shouldHandleNullIntegerRemainingSpins() {
        SpinResultDTO result = SpinResultDTO.builder()
                .remainingSpins((Integer) null)
                .build();
        
        assertEquals(0L, result.getRemainingSpins());
    }

    @Test
    void shouldCopyWithBuilder() {
        SpinResultDTO original = SpinResultDTO.builder()
                .won(true)
                .rewardId(1L)
                .rewardName("Test Reward")
                .isGoldenHour(true)
                .multiplier(2.0)
                .remainingSpins(5L)
                .eventId(1L)
                .location("TEST")
                .build();

        SpinResultDTO copy = original.toBuilder()
                .remainingSpins(4L)
                .build();

        // Verify modified field
        assertEquals(4L, copy.getRemainingSpins());

        // Verify copied fields
        assertTrue(copy.getWon());
        assertEquals(1L, copy.getRewardId());
        assertEquals("Test Reward", copy.getRewardName());
        assertTrue(copy.getIsGoldenHour());
        assertEquals(2.0, copy.getMultiplier());
        assertEquals(1L, copy.getEventId());
        assertEquals("TEST", copy.getLocation());
    }

    @Test
    void shouldHandleSettersAndGetters() {
        SpinResultDTO result = new SpinResultDTO();
        
        result.setWon(true);
        result.setRewardId(1L);
        result.setRewardName("Test Reward");
        result.setIsGoldenHour(true);
        result.setMultiplier(2.0);
        result.setRemainingSpins(5L);
        result.setEventId(1L);
        result.setLocation("TEST");

        assertTrue(result.getWon());
        assertEquals(1L, result.getRewardId());
        assertEquals("Test Reward", result.getRewardName());
        assertTrue(result.getIsGoldenHour());
        assertEquals(2.0, result.getMultiplier());
        assertEquals(5L, result.getRemainingSpins());
        assertEquals(1L, result.getEventId());
        assertEquals("TEST", result.getLocation());
    }
}