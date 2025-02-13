package vn.com.fecredit.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.exception.BusinessException;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;
import vn.com.fecredit.app.service.SpinService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Spin Controller Tests")
public class SpinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpinService spinService;

    private SpinRequest spinRequest;
    private Participant testParticipant;
    private Reward testReward;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testParticipant = createTestParticipant();
        testReward = createTestReward();
        spinRequest = createTestSpinRequest();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should perform spin when request is valid")
    void shouldPerformSpinWhenRequestIsValid() throws Exception {
        SpinHistory spinHistory = createTestSpinHistory(testParticipant, testReward);
        when(spinService.spin(any(SpinRequest.class))).thenReturn(spinHistory);

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(spinRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participant.id").value(testParticipant.getId()))
                .andExpect(jsonPath("$.participant.fullName").value(testParticipant.getFullName()))
                .andExpect(jsonPath("$.spinTime").exists())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.reward.name").value(testReward.getName()));

        verify(spinService).spin(any(SpinRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 when participant does not exist")
    void shouldReturn400WhenParticipantDoesNotExist() throws Exception {
        when(spinService.spin(any(SpinRequest.class)))
                .thenThrow(new BusinessException("Participant not found"));

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(spinRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Participant not found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return history when spins exist")
    void shouldReturnHistoryWhenSpinsExist() throws Exception {
        SpinHistory spinHistory = createTestSpinHistory(testParticipant, testReward);
        when(spinService.getLatestSpinHistory(testParticipant.getId()))
                .thenReturn(spinHistory);

        mockMvc.perform(get("/api/spins/history/latest/{participantId}", testParticipant.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participant.id").value(testParticipant.getId()))
                .andExpect(jsonPath("$.participant.fullName").value(testParticipant.getFullName()))
                .andExpect(jsonPath("$.spinTime").exists())
                .andExpect(jsonPath("$.reward.name").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 when no history exists")
    void shouldReturn404WhenNoHistoryExists() throws Exception {
        when(spinService.getLatestSpinHistory(testParticipant.getId()))
                .thenReturn(null);

        mockMvc.perform(get("/api/spins/history/latest/{participantId}", testParticipant.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/spins/history/latest/{participantId}", testParticipant.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private Participant createTestParticipant() {
        return Participant.builder()
                .id(1L)
                .customerId("TEST123")
                .cardNumber("4111111111111111")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0987654321")
                .province("Test Province")
                .spinsRemaining(3L)
                .build();
    }

    private Reward createTestReward() {
        return Reward.builder()
                .id(1L)
                .name("Test Reward")
                .description("Test Description")
                .quantity(100)
                .remainingQuantity(50)
                .probability(0.5)
                .isActive(true)
                .build();
    }

    private SpinRequest createTestSpinRequest() {
        return SpinRequest.builder()
                .participantId(testParticipant.getId())
                .eventId(1L)
                .customerLocation("Test Location")
                .isGoldenHourEligible(true)
                .hasActiveParticipation(true)
                .remainingSpinsForParticipant(3L)
                .participantStatus("ACTIVE")
                .build();
    }

    private SpinHistory createTestSpinHistory(Participant participant, Reward reward) {
        return SpinHistory.builder()
                .id(1L)
                .participant(participant)
                .reward(reward)
                .spinTime(now)
                .won(true)
                .isGoldenHour(false)
                .currentMultiplier(1.0)
                .remainingSpins(2L)
                .build();
    }
}