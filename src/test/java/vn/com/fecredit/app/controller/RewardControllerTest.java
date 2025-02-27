package vn.com.fecredit.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.golden.CreateGoldenHourRequest;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.service.RewardService;

@WebMvcTest(RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RewardService rewardService;

    private Reward testReward;
    private Event testEvent;
    private GoldenHourDTO testGoldenHourDTO;
    private GoldenHourDTO.CreateRequest createRequest;
    private CreateGoldenHourRequest createRequestV2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .build();

        testReward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .build();

        testGoldenHourDTO = GoldenHourDTO.builder()
                .id(1L)
                .eventId(testEvent.getId())
                .rewardId(testReward.getId())
                .name("Test Golden Hour")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();

        createRequest = GoldenHourDTO.CreateRequest.builder()
                .eventId(testEvent.getId())
                .name("New Golden Hour")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .build();

        createRequestV2 = CreateGoldenHourRequest.builder()
                .eventId(testEvent.getId())
                .name("New Golden Hour V2")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRewardsByEventId_Success() throws Exception {
        when(rewardService.getRewardsByEventId(anyLong()))
                .thenReturn(Arrays.asList(testReward));

        mockMvc.perform(get("/api/v1/rewards/event/{eventId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRewardById_Success() throws Exception {
        when(rewardService.getRewardById(anyLong()))
                .thenReturn(Optional.of(testReward));

        mockMvc.perform(get("/api/v1/rewards/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuantity_Success() throws Exception {
        mockMvc.perform(put("/api/v1/rewards/{id}/quantity/{quantity}", 1L, 10))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addGoldenHour_Success() throws Exception {
        when(rewardService.addGoldenHour(anyLong(), any(GoldenHourDTO.CreateRequest.class)))
                .thenReturn(testGoldenHourDTO);

        mockMvc.perform(post("/api/v1/rewards/{id}/golden-hours", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addGoldenHourV2_Success() throws Exception {
        when(rewardService.addGoldenHour(anyLong(), any(CreateGoldenHourRequest.class)))
                .thenReturn(testGoldenHourDTO);

        mockMvc.perform(post("/api/v1/rewards/{id}/golden-hours/v2", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestV2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeGoldenHour_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/rewards/{rewardId}/golden-hours/{goldenHourId}", 1L, 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/rewards/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addGoldenHour_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/{id}/golden-hours", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}