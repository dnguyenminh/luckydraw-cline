package vn.com.fecredit.app.dto.spin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinRequest {
    @NotNull(message = "Participant ID is required")
    private Long participantId;

    @NotNull(message = "Event code is required")
    private String eventCode;
}