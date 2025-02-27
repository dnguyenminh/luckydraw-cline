package vn.com.fecredit.app.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinResultResponse {

    private Long id;
    private Long eventId;
    private Long eventLocationId;
    private Long participantId;
    private Long rewardId;
    private String rewardName;
    private Integer rewardValue;
    private LocalDateTime spinTime;
    private LocalDateTime spinDate;
    private Boolean isSuccess;
    private Boolean isWin;
    private Boolean isGoldenHourActive;
    private Integer multiplier;
    private Long goldenHourId;

    @Builder.Default
    private String message = "Spin completed successfully";

    public static SpinResultResponse failed(String message) {
        return SpinResultResponse.builder()
                .isSuccess(false)
                .isWin(false)
                .message(message)
                .build();
    }

    public static SpinResultResponse success(Long rewardId, String rewardName, Integer rewardValue) {
        return SpinResultResponse.builder()
                .rewardId(rewardId)
                .rewardName(rewardName)
                .rewardValue(rewardValue)
                .isSuccess(true)
                .isWin(true)
                .build();
    }

    public static SpinResultResponse noWin() {
        return SpinResultResponse.builder()
                .isSuccess(true) 
                .isWin(false)
                .message("Better luck next time!")
                .build();
    }

    @Builder.Default 
    private SpinHistoryDetails history = new SpinHistoryDetails();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinHistoryDetails {
        private Long totalSpins;
        private Long dailySpins;
        private Long remainingSpins;
        private Long goldenHourSpins;
        private Long winningSpins;
    }
}
