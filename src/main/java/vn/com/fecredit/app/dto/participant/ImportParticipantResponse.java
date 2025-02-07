package vn.com.fecredit.app.dto.participant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportParticipantResponse {
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private List<String> errors;
    private List<String> successMessages;
}