package vn.com.fecredit.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Role;
import vn.com.fecredit.app.model.User;

class ParticipantMapperTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private ParticipantMapper mapper;

    private Event testEvent;
    private EventLocation testLocation;
    private Set<Role> testRoles;
    private RoleDTO testRoleDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        testLocation = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .province("Test Province")
                .totalSpins(100)
                .remainingSpins(100)
                .dailySpinLimit(3)
                .spinsRemaining(1000L)
                .isActive(true)
                .build();

        testRoles = new HashSet<>();
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_TEST");
        testRoles.add(role);

        testRoleDTO = RoleDTO.builder()
                .id(1L)
                .name("ROLE_TEST")
                .build();
    }

    @Test
    void toDTO_ShouldMapAllFields() {
        // Given
        Participant participant = Participant.builder()
                .id(1L)
                .event(testEvent)
                .eventLocation(testLocation)
                .customerId("CUST123")
                .cardNumber("4111111111111111")
                .fullName("Test User")
                .email("test@example.com")
                .phoneNumber("0123456789")
                .province("Test Province")
                .isActive(true)
                .isEligibleForSpin(true)
                .spinsRemaining(3)
                .dailySpinLimit(3)
                .roles(testRoles)
                .build();

        when(roleMapper.toDTO(any(Role.class))).thenReturn(testRoleDTO);

        // When
        ParticipantDTO dto = mapper.toDTO(participant);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(participant.getId());
        assertThat(dto.getEventId()).isEqualTo(testEvent.getId());
        assertThat(dto.getEventName()).isEqualTo(testEvent.getName());
        assertThat(dto.getEventLocationId()).isEqualTo(testLocation.getId());
        assertThat(dto.getEventLocationName()).isEqualTo(testLocation.getName());
        assertThat(dto.getEventLocationProvince()).isEqualTo(testLocation.getProvince());
        assertThat(dto.getCustomerId()).isEqualTo(participant.getCustomerId());
        assertThat(dto.getCardNumber()).isEqualTo(participant.getCardNumber());
        assertThat(dto.getFullName()).isEqualTo(participant.getFullName());
        assertThat(dto.getEmail()).isEqualTo(participant.getEmail());
        assertThat(dto.getPhoneNumber()).isEqualTo(participant.getPhoneNumber());
        assertThat(dto.getProvince()).isEqualTo(participant.getProvince());
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateAllFields() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .customerId("NEW_CUST123")
                .cardNumber("4222222222222222")
                .fullName("Updated User")
                .email("updated@example.com")
                .phoneNumber("9876543210")
                .province("Updated Province")
                .spinsRemaining(5)
                .dailySpinLimit(5)
                .isActive(false)
                .isEligibleForSpin(false)
                .build();

        Participant participant = Participant.builder()
                .id(1L)
                .event(testEvent)
                .eventLocation(testLocation)
                .customerId("CUST123")
                .cardNumber("4111111111111111")
                .fullName("Test User")
                .email("test@example.com")
                .phoneNumber("0123456789")
                .province("Test Province")
                .isActive(true)
                .isEligibleForSpin(true)
                .spinsRemaining(3)
                .dailySpinLimit(3)
                .roles(testRoles)
                .build();

        // When
        mapper.updateEntityFromDTO(dto, participant);

        // Then
        assertThat(participant.getCustomerId()).isEqualTo(dto.getCustomerId());
        assertThat(participant.getCardNumber()).isEqualTo(dto.getCardNumber());
        assertThat(participant.getFullName()).isEqualTo(dto.getFullName());
        assertThat(participant.getEmail()).isEqualTo(dto.getEmail());
        assertThat(participant.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
        assertThat(participant.getProvince()).isEqualTo(dto.getProvince());
        assertThat(participant.getSpinsRemaining()).isEqualTo(dto.getSpinsRemaining());
        assertThat(participant.getDailySpinLimit()).isEqualTo(dto.getDailySpinLimit());
        assertThat(participant.isActive()).isEqualTo(dto.getIsActive());
        assertThat(participant.isEligibleForSpin()).isEqualTo(dto.getIsEligibleForSpin());
    }
}