package vn.com.fecredit.app.dto.participant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateParticipantRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String cardNumber;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    @Pattern(regexp = "^(0[35789][0-9]{8}|84[35789][0-9]{8})$", message = "Must be a valid Vietnamese phone number")
    private String phoneNumber;
    private String province;

    @NotNull
    @Min(1)
    private Long dailySpinLimit;

    @NotNull
    private Long eventId;
}