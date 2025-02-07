package vn.com.fecredit.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.dto.participant.CreateParticipantRequest;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.repository.EventRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
public class ParticipantControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    private CreateParticipantRequest createRequest;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Create test event
        testEvent = Event.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .build();
        testEvent = eventRepository.save(testEvent);

        // Create participant request
        createRequest = CreateParticipantRequest.builder()
                .customerId("TEST123")
                .cardNumber("4111111111111111")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0123456789")
                .province("Test Province")
                .dailySpinLimit(3)
                .eventId(testEvent.getId())
                .build();
    }

    @Test
    void createParticipant_ValidInput_Returns201() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value(createRequest.getFullName()))
                .andExpect(jsonPath("$.phoneNumber").value(createRequest.getPhoneNumber()))
                .andExpect(jsonPath("$.email").value(createRequest.getEmail()))
                .andExpect(jsonPath("$.province").value(createRequest.getProvince()));
    }

    @Test
    void createParticipant_InvalidInput_Returns400() throws Exception {
        createRequest.setEmail("invalid-email");
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createParticipant_WithUserRole_Returns403() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void getParticipant_ExistingId_Returns200() throws Exception {
        // First create a participant
        String json = objectMapper.writeValueAsString(createRequest);
        String responseContent = mockMvc.perform(post("/api/v1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString();

        // Extract id from response
        Long participantId = objectMapper.readTree(responseContent).get("id").asLong();

        // Then get the participant
        mockMvc.perform(get("/api/v1/participants/{id}", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(createRequest.getFullName()))
                .andExpect(jsonPath("$.phoneNumber").value(createRequest.getPhoneNumber()))
                .andExpect(jsonPath("$.email").value(createRequest.getEmail()))
                .andExpect(jsonPath("$.province").value(createRequest.getProvince()));
    }

    @Test
    void getParticipant_NonExistingId_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/participants/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getParticipant_WithUserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/participants/{id}", 1L))
                .andExpect(status().isForbidden());
    }
}