package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.User;

@ExtendWith(MockitoExtension.class)
class ParticipantMapperTest {

    @InjectMocks
    private ParticipantMapper mapper;

    @Test
    void toDTO_ShouldMapAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Event event = Event.builder()
            .id(1L)
            .name("Test Event")
            .startDate(now.minusDays(1))
            .endDate(now.plusDays(1))
            .isActive(true)
            .build();

        EventLocation location = EventLocation.builder()
            .id(1L)
            .name("Test Location")
            .location("Test Address")
            .totalSpins(100L)
            .remainingSpins(50L)
            .isActive(true)
            .build();

        User user = User.builder()
            .id(1L)
            .username("testuser")
            .build();

        Participant participant = Participant.builder()
            .id(1L)
            .event(event)
            .eventLocation(location)
            .user(user)
            .name("Test Participant")
            .fullName("Test Full Name")
            .email("test@example.com")
            .phoneNumber("1234567890")
            .province("Test Province")
            .customerId("CUST001")
            .employeeId("EMP001")
            .cardNumber("CARD001")
            .spinsRemaining(10L)
            .dailySpinLimit(5L)
            .isActive(true)
            .isEligibleForSpin(true)
            .build();

        // When
        ParticipantDTO dto = mapper.toDTO(participant);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(participant.getId());
        assertThat(dto.getEventId()).isEqualTo(event.getId());
        assertThat(dto.getEventName()).isEqualTo(event.getName());
        assertThat(dto.getEventStartDate()).isEqualTo(event.getStartDate());
        assertThat(dto.getEventEndDate()).isEqualTo(event.getEndDate());
        assertThat(dto.getEventLocationId()).isEqualTo(location.getId());
        assertThat(dto.getEventLocationName()).isEqualTo(location.getName());
        assertThat(dto.getLocation()).isEqualTo(location.getLocation());
        assertThat(dto.getLocationTotalSpins()).isEqualTo(location.getTotalSpins());
        assertThat(dto.getLocationRemainingSpins()).isEqualTo(location.getRemainingSpins());
        assertThat(dto.getUserId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(participant.getName());
        assertThat(dto.getFullName()).isEqualTo(participant.getFullName());
        assertThat(dto.getEmail()).isEqualTo(participant.getEmail());
        assertThat(dto.getPhoneNumber()).isEqualTo(participant.getPhoneNumber());
        assertThat(dto.getProvince()).isEqualTo(participant.getProvince());
        assertThat(dto.getCustomerId()).isEqualTo(participant.getCustomerId());
        assertThat(dto.getEmployeeId()).isEqualTo(participant.getEmployeeId());
        assertThat(dto.getCardNumber()).isEqualTo(participant.getCardNumber());
        assertThat(dto.getSpinsRemaining()).isEqualTo(participant.getSpinsRemaining());
        assertThat(dto.getDailySpinLimit()).isEqualTo(participant.getDailySpinLimit());
        assertThat(dto.isActive()).isEqualTo(participant.isActive());
        assertThat(dto.isEligibleForSpin()).isEqualTo(participant.isEligibleForSpin());
    }
}