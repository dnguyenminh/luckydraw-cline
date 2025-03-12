package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @Captor
    private ArgumentCaptor<BlacklistedToken> tokenCaptor;

    private String testToken;
    private String testTokenHash;
    private long testExpirationTime;

    @BeforeEach
    void setUp() {
        testToken = "test.jwt.token";
        testTokenHash = tokenBlacklistService.hashToken(testToken);
        testExpirationTime = Instant.now().plusSeconds(3600).toEpochMilli();
    }

    @Test
    void blacklist_WhenTokenNotBlacklisted_ShouldSaveToken() {
        // Given
        when(blacklistedTokenRepository.existsByTokenHash(testTokenHash)).thenReturn(false);

        // When
        tokenBlacklistService.blacklist(testToken, false, testExpirationTime, "admin", "logout");

        // Then
        verify(blacklistedTokenRepository).save(tokenCaptor.capture());
        BlacklistedToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getTokenHash()).isEqualTo(testTokenHash);
        assertThat(savedToken.getExpirationTime()).isEqualTo(testExpirationTime);
        assertThat(savedToken.getRevokedBy()).isEqualTo("admin");
        assertThat(savedToken.getRevocationReason()).isEqualTo("logout");
        assertThat(savedToken.isRefreshToken()).isFalse();
    }

    @Test
    void blacklist_WhenTokenAlreadyBlacklisted_ShouldNotSaveToken() {
        // Given
        when(blacklistedTokenRepository.existsByTokenHash(testTokenHash)).thenReturn(true);

        // When
        tokenBlacklistService.blacklist(testToken, false, testExpirationTime);

        // Then
        verify(blacklistedTokenRepository, never()).save(any());
    }

    @Test
    void isBlacklisted_WhenTokenIsBlacklistedAndValid_ShouldReturnTrue() {
        // Given
        when(blacklistedTokenRepository.isTokenBlacklistedAndValid(anyString(), anyLong()))
                .thenReturn(true);

        // When
        boolean result = tokenBlacklistService.isBlacklisted(testToken);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void removeFromBlacklist_WhenTokenExists_ShouldReturnTrue() {
        // Given
        when(blacklistedTokenRepository.removeByTokenHash(testTokenHash)).thenReturn(1);

        // When
        boolean result = tokenBlacklistService.removeFromBlacklist(testToken);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void removeFromBlacklist_WhenTokenDoesNotExist_ShouldReturnFalse() {
        // Given
        when(blacklistedTokenRepository.removeByTokenHash(testTokenHash)).thenReturn(0);

        // When
        boolean result = tokenBlacklistService.removeFromBlacklist(testToken);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getTokensExpiringBetween_ShouldReturnTokensInRange() {
        // Given
        long startTime = Instant.now().toEpochMilli();
        long endTime = startTime + 3600000;
        List<BlacklistedToken> expectedTokens = Arrays.asList(
            BlacklistedToken.builder().tokenHash("hash1").build(),
            BlacklistedToken.builder().tokenHash("hash2").build()
        );
        when(blacklistedTokenRepository.findTokensExpiringBetween(startTime, endTime))
                .thenReturn(expectedTokens);

        // When
        List<BlacklistedToken> result = tokenBlacklistService.getTokensExpiringBetween(startTime, endTime);

        // Then
        assertThat(result).hasSize(2).isEqualTo(expectedTokens);
    }

    @Test
    void cleanupExpiredTokens_WhenExpiredTokensExist_ShouldDeleteAndReturnCount() {
        // Given
        List<BlacklistedToken> expiredTokens = Arrays.asList(
            BlacklistedToken.builder().tokenHash("hash1").expirationTime(1000L).build(),
            BlacklistedToken.builder().tokenHash("hash2").expirationTime(2000L).build()
        );
        when(blacklistedTokenRepository.findExpiredTokens(any())).thenReturn(expiredTokens);
        when(blacklistedTokenRepository.deleteExpiredTokens(any())).thenReturn(2);

        // When
        int deletedCount = tokenBlacklistService.cleanupExpiredTokens();

        // Then
        assertThat(deletedCount).isEqualTo(2);
        verify(blacklistedTokenRepository).deleteExpiredTokens(any());
    }

    @Test
    void hashToken_ShouldReturnConsistentHash() {
        // When
        String hash1 = tokenBlacklistService.hashToken(testToken);
        String hash2 = tokenBlacklistService.hashToken(testToken);

        // Then
        assertThat(hash1)
                .isNotEmpty()
                .hasSize(32)
                .isEqualTo(hash2);
    }

    @Test
    void countByType_ShouldReturnCorrectCount() {
        // Given
        when(blacklistedTokenRepository.countByType(true)).thenReturn(5L);
        when(blacklistedTokenRepository.countByType(false)).thenReturn(10L);

        // When
        long refreshTokenCount = tokenBlacklistService.countByType(true);
        long accessTokenCount = tokenBlacklistService.countByType(false);

        // Then
        assertThat(refreshTokenCount).isEqualTo(5L);
        assertThat(accessTokenCount).isEqualTo(10L);
    }
}
