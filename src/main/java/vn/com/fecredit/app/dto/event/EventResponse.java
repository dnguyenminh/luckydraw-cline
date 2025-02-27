package vn.com.fecredit.app.dto.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.dto.RewardDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer dailySpinLimit;
    private Integer totalSpins;
    private Integer remainingSpins;
    private boolean active;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EventStatistics statistics;
    private List<EventLocationDTO.EventLocationResponse> locations;
    private List<RewardDTO.RewardResponse> rewards;
}
