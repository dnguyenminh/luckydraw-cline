package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.dto.participant.UpdateParticipantRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.ParticipantMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventRepository eventRepository;

    private ParticipantMapper participantMapper;
    private ParticipantService participantService;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        participantMapper = new ParticipantMapper();
        participantService = new ParticipantService(participantRepository, eventRepository, participantMapper);
    }

    @Nested
    @DisplayName("Create Participant Tests")
    class CreateParticipantTests {

        @Test
        @DisplayName("Should create participant when input is valid")
        void shouldCreateParticipantWhenInputIsValid() {
            // Given
            CreateParticipantRequest request = createValidRequest();
            Event event = createValidEvent();
            Participant participant = createValidParticipant(event);

            when(eventRepository.findById(request.getEventId())).thenReturn(Optional.of(event));
            when(participantRepository.save(any(Participant.class))).thenReturn(participant);

            // When
            ParticipantDTO result = participantService.createParticipant(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(participant.getCustomerId());
            assertThat(result.getFullName()).isEqualTo(participant.getFullName());
            assertThat(result.getEmail()).isEqualTo(participant.getEmail());
            assertThat(result.getEventId()).isEqualTo(event.getId());
            verify(participantRepository).save(any(Participant.class));
            verify(eventRepository).findById(request.getEventId());
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void shouldThrowExceptionWhenEventNotFound() {
            // Given
            CreateParticipantRequest request = createValidRequest();
            when(eventRepository.findById(request.getEventId())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> participantService.createParticipant(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Event not found");
        }
    }

    @Nested
    @DisplayName("Update Participant Tests")
    class UpdateParticipantTests {

        @Test
        @DisplayName("Should update participant when input is valid")
        void shouldUpdateParticipantWhenInputIsValid() {
            // Given
            Long participantId = 1L;
            UpdateParticipantRequest request = createValidUpdateRequest();
            Participant existingParticipant = createValidParticipant(createValidEvent());

            when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
            when(participantRepository.save(any(Participant.class))).thenReturn(existingParticipant);

            // When
            ParticipantDTO result = participantService.updateParticipant(participantId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo(existingParticipant.getFullName());
            assertThat(result.getEmail()).isEqualTo(existingParticipant.getEmail());
            verify(participantRepository).save(any(Participant.class));
        }

        @Test
        @DisplayName("Should throw exception when participant not found")
        void shouldThrowExceptionWhenParticipantNotFound() {
            // Given
            Long participantId = 999L;
            UpdateParticipantRequest request = createValidUpdateRequest();
            when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> participantService.updateParticipant(participantId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Participant not found");
        }
    }

    @Test
    @DisplayName("Should get all participants with search and pagination")
    void shouldGetAllParticipantsWithSearchAndPagination() {
        // Given
        String search = "test";
        Pageable pageable = PageRequest.of(0, 10);
        List<Participant> participants = Arrays.asList(
            createValidParticipant(createValidEvent()),
            createValidParticipant(createValidEvent())
        );
        Page<Participant> participantPage = new PageImpl<>(participants);

        when(participantRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(participantPage);

        // When
        Page<ParticipantDTO> result = participantService.getAllParticipants(search, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(participants.get(0).getCustomerId());
        assertThat(result.getContent().get(1).getCustomerId()).isEqualTo(participants.get(1).getCustomerId());
        verify(participantRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should find participant by customer ID")
    void shouldFindParticipantByCustomerId() {
        // Given
        String customerId = "TEST123";
        Participant participant = createValidParticipant(createValidEvent());
        when(participantRepository.findByCustomerId(customerId)).thenReturn(Optional.of(participant));

        // When
        Participant result = participantService.findByCustomerId(customerId);

        // Then
        assertThat(result).isEqualTo(participant);
        verify(participantRepository).findByCustomerId(customerId);
    }

    private CreateParticipantRequest createValidRequest() {
        return CreateParticipantRequest.builder()
                .customerId("TEST123")
                .cardNumber("4111111111111111")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0987654321")
                .province("Test Province")
                .dailySpinLimit(3L)
                .eventId(1L)
                .build();
    }

    private UpdateParticipantRequest createValidUpdateRequest() {
        return UpdateParticipantRequest.builder()
                .fullName("Updated User")
                .email("updated@example.com")
                .phoneNumber("0987654322")
                .province("Updated Province")
                .dailySpinLimit(5L)
                .isActive(true)
                .build();
    }

    private Event createValidEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .build();
    }

    private Participant createValidParticipant(Event event) {
        return Participant.builder()
                .id(1L)
                .customerId("TEST123")
                .cardNumber("4111111111111111")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0987654321")
                .province("Test Province")
                .dailySpinLimit(3L)
                .spinsRemaining(3L)
                .event(event)
                .isActive(true)
                .build();
    }
}