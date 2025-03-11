package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken extends AbstractStatusAwareEntity {

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_EXPIRED = 0;
    public static final int STATUS_REVOKED = -1;

    @Column(nullable = false, unique = true)
    private String token;

    private String tokenType;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private String reason;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isActive() {
        return STATUS_ACTIVE == this.status && !isExpired();
    }

    public void markAsExpired() {
        this.status = STATUS_EXPIRED;
    }

    public void markAsRevoked(String reason) {
        this.status = STATUS_REVOKED;
        this.reason = reason;
    }
}
