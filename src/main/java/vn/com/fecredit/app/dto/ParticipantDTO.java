package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {
    private Long id;
    private String customerId;
    private String cardNumber;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String province;
    private Long dailySpinLimit;
    private Long spinsRemaining;
    private Boolean isActive;
    private Long eventId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Default
    private Boolean isEligibleForSpin = true;
}