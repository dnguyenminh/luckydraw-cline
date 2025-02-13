package vn.com.fecredit.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.service.RewardService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Reward Controller Tests")
public class RewardControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RewardService rewardService;

    private RewardDTO.CreateRewardRequest createRequest;
    private Long testEventId;

    @BeforeEach
    void setUp() {
        testEventId = 1L;
        createRequest = createTestRewardRequest(testEventId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create reward when input is valid")
    void shouldCreateRewardWhenInputIsValid() throws Exception {
        RewardDTO expectedResponse = createExpectedRewardResponse(1L, createRequest);
        when(rewardService.createReward(any(RewardDTO.CreateRewardRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(createRequest.getName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when creating reward with non-existent event")
    void shouldReturn404WhenCreatingRewardWithNonExistentEvent() throws Exception {
        when(rewardService.createReward(any(RewardDTO.CreateRewardRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event", "id", testEventId));

        mockMvc.perform(post("/api/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return all rewards")
    void shouldReturnAllRewards() throws Exception {
        List<RewardDTO> allRewards = Arrays.asList(
            createExpectedRewardResponse(1L, createRequest),
            createExpectedRewardResponse(2L, createRequest)
        );

        when(rewardService.getAllRewards()).thenReturn(allRewards);

        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return reward by ID when exists")
    void shouldReturnRewardByIdWhenExists() throws Exception {
        Long rewardId = 1L;
        RewardDTO reward = createExpectedRewardResponse(rewardId, createRequest);
        when(rewardService.getRewardById(rewardId)).thenReturn(reward);

        mockMvc.perform(get("/api/rewards/{id}", rewardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rewardId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when reward ID not found")
    void shouldReturn404WhenRewardIdNotFound() throws Exception {
        Long rewardId = 999L;
        when(rewardService.getRewardById(rewardId))
                .thenThrow(new ResourceNotFoundException("Reward", "id", rewardId));

        mockMvc.perform(get("/api/rewards/{id}", rewardId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update reward quantity")
    void shouldUpdateRewardQuantity() throws Exception {
        Long rewardId = 1L;
        int newQuantity = 50;
        RewardDTO updatedReward = createExpectedRewardResponse(rewardId, createRequest);
        updatedReward.setQuantity(newQuantity);

        when(rewardService.updateQuantity(rewardId, newQuantity)).thenReturn(updatedReward);

        mockMvc.perform(put("/api/rewards/{id}/quantity", rewardId)
                .param("quantity", String.valueOf(newQuantity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(newQuantity));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should add golden hour to reward")
    void shouldAddGoldenHourToReward() throws Exception {
        Long rewardId = 1L;
        GoldenHourDTO.CreateRequest goldenHourRequest = createGoldenHourRequest();
        RewardDTO updatedReward = createExpectedRewardResponse(rewardId, createRequest);

        when(rewardService.addGoldenHour(eq(rewardId), any(GoldenHourDTO.CreateRequest.class)))
                .thenReturn(updatedReward);

        mockMvc.perform(post("/api/rewards/{id}/golden-hours", rewardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(goldenHourRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rewardId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when adding invalid golden hour")
    void shouldReturn400WhenAddingInvalidGoldenHour() throws Exception {
        Long rewardId = 1L;
        GoldenHourDTO.CreateRequest invalidRequest = GoldenHourDTO.CreateRequest.builder()
                .name("") // Invalid: name should not be blank
                .multiplier(-1.0) // Invalid: multiplier should be positive
                .startTime(null) // Invalid: startTime is required
                .endTime(null) // Invalid: endTime is required
                .build();

        mockMvc.perform(post("/api/rewards/{id}/golden-hours", rewardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when adding golden hour to non-existent reward")
    void shouldReturn404WhenAddingGoldenHourToNonExistentReward() throws Exception {
        Long rewardId = 999L;
        GoldenHourDTO.CreateRequest goldenHourRequest = createGoldenHourRequest();

        when(rewardService.addGoldenHour(eq(rewardId), any(GoldenHourDTO.CreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Reward", "id", rewardId));

        mockMvc.perform(post("/api/rewards/{id}/golden-hours", rewardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(goldenHourRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should remove golden hour from reward")
    void shouldRemoveGoldenHourFromReward() throws Exception {
        Long rewardId = 1L;
        Long goldenHourId = 2L;
        RewardDTO updatedReward = createExpectedRewardResponse(rewardId, createRequest);

        when(rewardService.removeGoldenHour(rewardId, goldenHourId)).thenReturn(updatedReward);

        mockMvc.perform(delete("/api/rewards/{rewardId}/golden-hours/{goldenHourId}", rewardId, goldenHourId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rewardId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when removing non-existent golden hour")
    void shouldReturn404WhenRemovingNonExistentGoldenHour() throws Exception {
        Long rewardId = 1L;
        Long goldenHourId = 999L;

        when(rewardService.removeGoldenHour(rewardId, goldenHourId))
                .thenThrow(new ResourceNotFoundException("GoldenHour", "id", goldenHourId));

        mockMvc.perform(delete("/api/rewards/{rewardId}/golden-hours/{goldenHourId}", rewardId, goldenHourId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete reward when exists")
    void shouldDeleteRewardWhenExists() throws Exception {
        Long rewardId = 1L;

        mockMvc.perform(delete("/api/rewards/{id}", rewardId))
                .andExpect(status().isNoContent());

        verify(rewardService).deleteReward(rewardId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent reward")
    void shouldReturn404WhenDeletingNonExistentReward() throws Exception {
        Long rewardId = 999L;
        doThrow(new ResourceNotFoundException("Reward", "id", rewardId))
                .when(rewardService).deleteReward(rewardId);

        mockMvc.perform(delete("/api/rewards/{id}", rewardId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isUnauthorized());
    }

    private RewardDTO.CreateRewardRequest createTestRewardRequest(Long eventId) {
        return RewardDTO.CreateRewardRequest.builder()
                .name("Test Reward")
                .description("Test Reward Description")
                .quantity(100)
                .probability(0.5)
                .maxQuantityInPeriod(10)
                .eventId(eventId)
                .isActive(true)
                .build();
    }

    private GoldenHourDTO.CreateRequest createGoldenHourRequest() {
        return GoldenHourDTO.CreateRequest.builder()
                .name("Test Golden Hour")
                .multiplier(2.0)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isActive(true)
                .build();
    }

    private RewardDTO createExpectedRewardResponse(Long id, RewardDTO.CreateRewardRequest request) {
        return RewardDTO.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .probability(request.getProbability())
                .maxQuantityInPeriod(request.getMaxQuantityInPeriod())
                .eventId(request.getEventId())
                .isActive(request.getIsActive())
                .build();
    }
}