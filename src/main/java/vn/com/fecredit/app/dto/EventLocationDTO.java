package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLocationDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private String name;
    private String location;
    private Long totalSpins;
    private Long remainingSpins;
    private boolean active;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}