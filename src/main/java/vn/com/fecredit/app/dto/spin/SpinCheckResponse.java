package vn.com.fecredit.app.dto.spin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinCheckResponse {
    private boolean eligible;
    private String message;
}