package vn.com.fecredit.app.dto.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummary {
    private Long id;
    private String code;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer remainingSpins;
    private boolean active;
    private EventStatistics statistics;
}
