package vn.com.fecredit.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.service.ParticipantService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Participant Controller Test")
class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipantService participantService;

    private CreateParticipantRequest createRequest;
    private ParticipantDTO expectedResponse;

    @BeforeEach
    void setUp() {
        createRequest = CreateParticipantRequest.builder()
                .customerId("TEST123")
                .cardNumber("4111111111111111")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0987654321")
                .province("Test Province")
                .dailySpinLimit(3L)
                .eventId(1L)
                .build();

        expectedResponse = ParticipantDTO.builder()
                .id(1L)
                .customerId(createRequest.getCustomerId())
                .cardNumber(createRequest.getCardNumber())
                .email(createRequest.getEmail())
                .fullName(createRequest.getFullName())
                .phoneNumber(createRequest.getPhoneNumber())
                .province(createRequest.getProvince())
                .dailySpinLimit(createRequest.getDailySpinLimit())
                .spinsRemaining(createRequest.getDailySpinLimit())
                .eventId(createRequest.getEventId())
                .isActive(true)
                .isEligibleForSpin(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateParticipantWhenInputIsValid() throws Exception {
        when(participantService.createParticipant(any(CreateParticipantRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(createRequest.getCustomerId()))
                .andExpect(jsonPath("$.dailySpinLimit").value(createRequest.getDailySpinLimit()))
                .andExpect(jsonPath("$.spinsRemaining").value(createRequest.getDailySpinLimit()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        createRequest.setEmail("invalid-email");
        
        mockMvc.perform(post("/api/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnParticipantWhenIdExists() throws Exception {
        when(participantService.getParticipant(1L)).thenReturn(expectedResponse);

        mockMvc.perform(get("/api/participants/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(expectedResponse.getCustomerId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenIdDoesNotExist() throws Exception {
        Long nonExistingId = 999L;
        when(participantService.getParticipant(nonExistingId))
                .thenThrow(new ResourceNotFoundException("Participant", "id", nonExistingId));

        mockMvc.perform(get("/api/participants/{id}", nonExistingId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/participants/{id}", 1L))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenInsufficientPrivileges() throws Exception {
        mockMvc.perform(get("/api/participants/{id}", 1L))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}