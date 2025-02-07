package vn.com.fecredit.app.dto.participant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParticipantRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;
    private String province;

    @NotNull
    @Min(1)
    private Integer dailySpinLimit;

    @NotNull
    private Boolean isActive;
}