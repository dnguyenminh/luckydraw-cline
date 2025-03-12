package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.BlacklistedToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByTokenHash(String tokenHash);

    Optional<BlacklistedToken> findByTokenHash(String tokenHash);

    @Query("SELECT b FROM BlacklistedToken b WHERE b.expirationTime < :currentTime")
    List<BlacklistedToken> findExpiredTokens(@Param("currentTime") Long currentTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedToken b WHERE b.expirationTime < :currentTime")
    int deleteExpiredTokens(@Param("currentTime") Long currentTime);

    @Query("SELECT COUNT(b) FROM BlacklistedToken b WHERE b.refreshToken = :isRefreshToken")
    long countByType(@Param("isRefreshToken") boolean isRefreshToken);

    @Query("""
            SELECT b FROM BlacklistedToken b 
            WHERE b.expirationTime BETWEEN :startTime AND :endTime 
            ORDER BY b.expirationTime DESC
            """)
    List<BlacklistedToken> findTokensExpiringBetween(
            @Param("startTime") Long startTime, 
            @Param("endTime") Long endTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedToken b WHERE b.tokenHash = :tokenHash")
    int removeByTokenHash(@Param("tokenHash") String tokenHash);

    @Query("""
            SELECT COUNT(b) > 0 FROM BlacklistedToken b 
            WHERE b.tokenHash = :tokenHash 
            AND b.expirationTime > :currentTime
            """)
    boolean isTokenBlacklistedAndValid(
            @Param("tokenHash") String tokenHash, 
            @Param("currentTime") Long currentTime);

    @Query("SELECT b.tokenHash FROM BlacklistedToken b WHERE b.revokedBy = :username")
    List<String> findTokenHashesRevokedBy(@Param("username") String username);
}
