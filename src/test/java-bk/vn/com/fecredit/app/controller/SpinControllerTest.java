package vn.com.fecredit.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.dto.SpinResultDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.exception.SpinNotAllowedException;
import vn.com.fecredit.app.service.SpinService;

@WebMvcTest(SpinController.class)
class SpinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpinService spinService;

    private SpinRequest validRequest;
    private SpinResultDTO successfulResult;

    @BeforeEach
    void setUp() {
        validRequest = SpinRequest.builder()
                .eventId(1L)
                .participantId(2L)
                .location("TEST")
                .hasActiveParticipation(true)
                .build();

        successfulResult = SpinResultDTO.builder()
                .won(true)
                .rewardId(3L)
                .rewardName("Test Reward")
                .remainingSpins(4L)
                .isGoldenHour(false)
                .multiplier(1.0)
                .eventId(1L)
                .location("TEST")
                .build();
    }

    @Test
    void spinWithValidRequestShouldSucceed() throws Exception {
        when(spinService.spinAndGetResult(any(SpinRequest.class))).thenReturn(successfulResult);

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardId").value(3))
                .andExpect(jsonPath("$.rewardName").value("Test Reward"))
                .andExpect(jsonPath("$.remainingSpins").value(4))
                .andExpect(jsonPath("$.isGoldenHour").value(false))
                .andExpect(jsonPath("$.multiplier").value(1.0))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.location").value("TEST"));
    }

    @Test
    void spinWithInvalidRequestShouldReturnBadRequest() throws Exception {
        SpinRequest invalidRequest = SpinRequest.builder().build();

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void spinWithNotAllowedConditionShouldReturnBadRequest() throws Exception {
        when(spinService.spinAndGetResult(any(SpinRequest.class)))
                .thenThrow(new SpinNotAllowedException("No remaining spins"));

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void spinWithNonExistentResourceShouldReturnNotFound() throws Exception {
        when(spinService.spinAndGetResult(any(SpinRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event not found"));

        mockMvc.perform(post("/api/spins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLatestSpinShouldReturnResult() throws Exception {
        when(spinService.getLatestSpinResult(2L)).thenReturn(successfulResult);

        mockMvc.perform(get("/api/spins/2/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardId").value(3))
                .andExpect(jsonPath("$.rewardName").value("Test Reward"));
    }

    @Test
    void getLatestSpinWithNoHistoryShouldReturnNotFound() throws Exception {
        when(spinService.getLatestSpinResult(2L)).thenReturn(null);

        mockMvc.perform(get("/api/spins/2/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processSpinShouldReturnResult() throws Exception {
        when(spinService.processSpin(2L)).thenReturn(successfulResult);

        mockMvc.perform(post("/api/spins/2/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardId").value(3))
                .andExpect(jsonPath("$.rewardName").value("Test Reward"));
    }

    @Test
    void processSpinWithInvalidParticipantShouldReturnNotFound() throws Exception {
        when(spinService.processSpin(2L))
                .thenThrow(new ResourceNotFoundException("Participant not found"));

        mockMvc.perform(post("/api/spins/2/process"))
                .andExpect(status().isNotFound());
    }
}