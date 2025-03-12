package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.dto.participant.UpdateParticipantRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.ParticipantMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;

class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventLocationRepository eventLocationRepository;

    @Mock
    private ParticipantMapper participantMapper;

    @InjectMocks
    private ParticipantService participantService;

    private Event testEvent;
    private EventLocation testLocation;
    private Participant testParticipant;
    private ParticipantDTO testParticipantDTO;
    private CreateParticipantRequest createRequest;
    private UpdateParticipantRequest updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testEvent = Event.builder()
            .id(1L)
            .name("Test Event")
            .code("TEST-EVENT")
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(30))
            .isActive(true)
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

        testParticipant = Participant.builder()
            .id(1L)
            .event(testEvent)
            .eventLocation(testLocation)
            .customerId("CUST123")
            .cardNumber("4111111111111111")
            .fullName("Test User")
            .email("test@example.com")
            .phoneNumber("0123456789")
            .province("Test Province")
            .dailySpinLimit(3)
            .spinsRemaining(3)
            .isActive(true)
            .isEligibleForSpin(true)
            .build();

        testParticipantDTO = ParticipantDTO.builder()
            .id(1L)
            .eventId(testEvent.getId())
            .eventLocationId(testLocation.getId())
            .customerId("CUST123")
            .cardNumber("4111111111111111")
            .fullName("Test User")
            .email("test@example.com")
            .phoneNumber("0123456789")
            .province("Test Province")
            .dailySpinLimit(3)
            .spinsRemaining(3)
            .isActive(true)
            .isEligibleForSpin(true)
            .build();

        createRequest = CreateParticipantRequest.builder()
            .eventId(testEvent.getId())
            .eventLocationId(testLocation.getId())
            .customerId("CUST123")
            .cardNumber("4111111111111111")
            .fullName("Test User")
            .email("test@example.com")
            .phoneNumber("0123456789")
            .province("Test Province")
            .dailySpinLimit(3)
            .spinsRemaining(3)
            .isActive(true)
            .isEligibleForSpin(true)
            .build();

        updateRequest = UpdateParticipantRequest.builder()
            .customerId("CUST123")
            .cardNumber("4111111111111111")
            .fullName("Test User")
            .email("test@example.com")
            .phoneNumber("0123456789")
            .province("Test Province")
            .dailySpinLimit(3)
            .spinsRemaining(3)
            .isActive(true)
            .isEligibleForSpin(true)
            .build();
    }

    @Test
    void createParticipant_Success() {
        when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
        when(eventLocationRepository.findById(testLocation.getId())).thenReturn(Optional.of(testLocation));
        when(participantRepository.save(any(Participant.class))).thenReturn(testParticipant);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(testParticipantDTO);

        ParticipantDTO result = participantService.createParticipant(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testParticipantDTO.getId());
    }

    @Test
    void updateParticipant_Success() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(testParticipant));
        when(participantRepository.save(any(Participant.class))).thenReturn(testParticipant);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(testParticipantDTO);

        ParticipantDTO result = participantService.updateParticipant(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testParticipantDTO.getId());
    }

    @Test
    void updateParticipant_NotFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            participantService.updateParticipant(1L, updateRequest);
        });
    }
}