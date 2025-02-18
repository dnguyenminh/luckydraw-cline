package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String department;
    private String position;

    /**
     * Creates a builder with current values.
     */
    public UserInfoDto.UserInfoDtoBuilder toBuilder() {
        return builder()
            .firstName(this.firstName)
            .lastName(this.lastName)
            .fullName(this.fullName)
            .phoneNumber(this.phoneNumber)
            .department(this.department)
            .position(this.position);
    }

    /**
     * Updates the fullName when firstName or lastName changes.
     */
    public void updateFullName() {
        this.fullName = String.format("%s %s", 
            firstName != null ? firstName : "",
            lastName != null ? lastName : "").trim();
    }
}