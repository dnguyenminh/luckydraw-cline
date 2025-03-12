package vn.com.fecredit.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.dto.participant.UpdateParticipantRequest;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.service.ParticipantService;

@WebMvcTest(ParticipantController.class)
class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipantService participantService;

    private Event testEvent;
    private EventLocation testLocation;
    private CreateParticipantRequest createRequest;
    private UpdateParticipantRequest updateRequest;
    private ParticipantDTO testParticipantDTO;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createParticipant_Success() throws Exception {
        when(participantService.createParticipant(any(CreateParticipantRequest.class)))
            .thenReturn(testParticipantDTO);

        mockMvc.perform(post("/api/v1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateParticipant_Success() throws Exception {
        when(participantService.updateParticipant(anyLong(), any(UpdateParticipantRequest.class)))
            .thenReturn(testParticipantDTO);

        mockMvc.perform(post("/api/v1/participants/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }
}