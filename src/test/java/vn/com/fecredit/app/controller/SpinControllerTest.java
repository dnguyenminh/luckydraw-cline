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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.dto.spin.SpinRequest;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
public class SpinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    private Event testEvent;
    private Participant testParticipant;
    private SpinRequest spinRequest;

    @BeforeEach
    void setUp() {
        // Clear any previous test data
        spinHistoryRepository.deleteAll();
        participantRepository.deleteAll();
        rewardRepository.deleteAll();
        eventRepository.deleteAll();

        // Create test event
        testEvent = Event.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .build();
        testEvent = eventRepository.save(testEvent);

        // Create test participant
        testParticipant = Participant.builder()
                .customerId("TEST123")
                .cardNumber("4111111111111111")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0123456789")
                .province("Test Province")
                .dailySpinLimit(3)
                .event(testEvent)
                .build();
        testParticipant = participantRepository.save(testParticipant);

        // Create test reward
        var rewardRequest = RewardDTO.CreateRewardRequest.builder()
                .name("Test Reward")
                .description("Test Reward Description")
                .quantity(100)
                .probability(0.5)
                .maxQuantityInPeriod(10)
                .eventId(testEvent.getId())
                .isActive(true)
                .build();

        // Create spin request
        spinRequest = SpinRequest.builder()
                .participantId(testParticipant.getId())
                .eventCode(testEvent.getCode())
                .build();
    }

    @Test
    void spin_ValidRequest_Returns200() throws Exception {
        String json = objectMapper.writeValueAsString(spinRequest);

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId").value(testParticipant.getId()))
                .andExpect(jsonPath("$.participantName").value(testParticipant.getFullName()))
                .andExpect(jsonPath("$.spinTime").exists())
                .andExpect(jsonPath("$.remainingSpins").isNumber());
    }

    @Test
    void spin_InvalidParticipantId_Returns404() throws Exception {
        spinRequest.setParticipantId(999L);
        String json = objectMapper.writeValueAsString(spinRequest);

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void spin_InvalidEventCode_Returns404() throws Exception {
        spinRequest.setEventCode("INVALID_EVENT");
        String json = objectMapper.writeValueAsString(spinRequest);

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void spin_SpinLimitExceeded_Returns400() throws Exception {
        String json = objectMapper.writeValueAsString(spinRequest);

        // Perform spins until limit is reached
        for (int i = 0; i < testParticipant.getDailySpinLimit(); i++) {
            mockMvc.perform(post("/api/spins")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk());
        }

        // Next spin should fail
        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Daily spin limit exceeded"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void spin_WithAdminRole_Returns403() throws Exception {
        String json = objectMapper.writeValueAsString(spinRequest);

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkSpinEligibility_ValidRequest_Returns200() throws Exception {
        String json = objectMapper.writeValueAsString(spinRequest);

        mockMvc.perform(post("/api/spins/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void checkSpinEligibility_LimitExceeded_ReturnsFalse() throws Exception {
        String json = objectMapper.writeValueAsString(spinRequest);

        // Perform spins until limit is reached
        for (int i = 0; i < testParticipant.getDailySpinLimit(); i++) {
            mockMvc.perform(post("/api/spins")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk());
        }

        // Check eligibility should return false
        mockMvc.perform(post("/api/spins/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.message").value("Daily spin limit exceeded"));
    }

    @Test
    void checkSpinEligibility_InvalidParticipantId_Returns404() throws Exception {
        spinRequest.setParticipantId(999L);
        String json = objectMapper.writeValueAsString(spinRequest);

        mockMvc.perform(post("/api/spins/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSpinHistory_ValidRequest_Returns200() throws Exception {
        // First perform a spin
        String json = objectMapper.writeValueAsString(spinRequest);
        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Then get spin history
        mockMvc.perform(get("/api/spins/{participantId}/history", testParticipant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId").value(testParticipant.getId()))
                .andExpect(jsonPath("$.participantName").value(testParticipant.getFullName()))
                .andExpect(jsonPath("$.spinTime").exists())
                .andExpect(jsonPath("$.remainingSpins").isNumber());
    }
}