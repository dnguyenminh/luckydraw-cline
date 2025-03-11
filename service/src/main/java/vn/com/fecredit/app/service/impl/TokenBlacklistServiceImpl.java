package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;
import vn.com.fecredit.app.service.TokenBlacklistService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    @Transactional
    public void blacklist(String token, boolean isRefreshToken, Long expirationTime) {
        blacklist(token, isRefreshToken, expirationTime, null, null);
    }

    @Override
    @Transactional
    public void blacklist(String token, boolean isRefreshToken, Long expirationTime, String revokedBy, String reason) {
        String tokenHash = hashToken(token);
        if (!blacklistedTokenRepository.existsByTokenHash(tokenHash)) {
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .tokenHash(tokenHash)
                    .refreshToken(isRefreshToken)
                    .expirationTime(expirationTime)
                    .revokedBy(revokedBy)
                    .revocationReason(reason)
                    .build();
            blacklistedTokenRepository.save(blacklistedToken);
            log.debug("Token blacklisted: isRefresh={}, revokedBy={}, reason={}", 
                    isRefreshToken, revokedBy, reason);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        String tokenHash = hashToken(token);
        return blacklistedTokenRepository.isTokenBlacklistedAndValid(tokenHash, Instant.now().toEpochMilli());
    }

    @Override
    @Transactional
    public boolean removeFromBlacklist(String token) {
        String tokenHash = hashToken(token);
        int removed = blacklistedTokenRepository.removeByTokenHash(tokenHash);
        if (removed > 0) {
            log.debug("Token removed from blacklist: hash={}", tokenHash);
            return true;
        }
        return false;
    }

    @Override
    public List<BlacklistedToken> getTokensExpiringBetween(Long startTime, Long endTime) {
        return blacklistedTokenRepository.findTokensExpiringBetween(startTime, endTime);
    }

    @Override
    public List<String> getTokensRevokedBy(String username) {
        return blacklistedTokenRepository.findTokenHashesRevokedBy(username);
    }

    @Override
    public long countByType(boolean isRefreshToken) {
        return blacklistedTokenRepository.countByType(isRefreshToken);
    }

    @Override
    public List<BlacklistedToken> getExpiredTokens() {
        return blacklistedTokenRepository.findExpiredTokens(Instant.now().toEpochMilli());
    }

    @Override
    @Transactional
    public int cleanupExpiredTokens() {
        long currentTime = Instant.now().toEpochMilli();
        List<BlacklistedToken> expiredTokens = blacklistedTokenRepository.findExpiredTokens(currentTime);
        int count = blacklistedTokenRepository.deleteExpiredTokens(currentTime);
        
        if (count > 0) {
            log.info("Cleaned up {} expired tokens", count);
            expiredTokens.forEach(token -> 
                log.debug("Removed expired token: hash={}, expiredAt={}, revokedBy={}", 
                    token.getTokenHash(), 
                    Instant.ofEpochMilli(token.getExpirationTime()), 
                    token.getRevokedBy()));
        }
        
        return count;
    }

    @Override
    public String hashToken(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}
