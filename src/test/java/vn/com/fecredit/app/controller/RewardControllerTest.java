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
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.repository.EventRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    private Event testEvent;
    private RewardDTO.CreateRewardRequest createRequest;

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

        // Create reward request
        createRequest = RewardDTO.CreateRewardRequest.builder()
                .name("Test Reward")
                .description("Test Reward Description")
                .quantity(100)
                .probability(0.5)
                .maxQuantityInPeriod(10)
                .eventId(testEvent.getId())
                .isActive(true)
                .build();
    }

    @Test
    void createReward_ValidInput_Returns201() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createRequest.getName()))
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                .andExpect(jsonPath("$.quantity").value(createRequest.getQuantity()))
                .andExpect(jsonPath("$.probability").value(createRequest.getProbability()))
                .andExpect(jsonPath("$.maxQuantityInPeriod").value(createRequest.getMaxQuantityInPeriod()));
    }

    @Test
    void createReward_InvalidInput_Returns400() throws Exception {
        createRequest.setQuantity(-1);
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createReward_WithUserRole_Returns403() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateReward_ExistingId_Returns200() throws Exception {
        // First create a reward
        String json = objectMapper.writeValueAsString(createRequest);
        String responseContent = mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString();

        // Extract id from response
        Long rewardId = objectMapper.readTree(responseContent).get("id").asLong();

        // Update request
        RewardDTO.UpdateRewardRequest updateRequest = RewardDTO.UpdateRewardRequest.builder()
                .name("Updated Reward")
                .description("Updated Description")
                .quantity(50)
                .probability(0.7)
                .maxQuantityInPeriod(5)
                .isActive(false)
                .build();

        // Then update the reward
        mockMvc.perform(put("/api/v1/rewards/{id}", rewardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.description").value(updateRequest.getDescription()))
                .andExpect(jsonPath("$.quantity").value(updateRequest.getQuantity()))
                .andExpect(jsonPath("$.probability").value(updateRequest.getProbability()))
                .andExpect(jsonPath("$.maxQuantityInPeriod").value(updateRequest.getMaxQuantityInPeriod()))
                .andExpect(jsonPath("$.isActive").value(updateRequest.getIsActive()));
    }

    @Test
    void updateReward_NonExistingId_Returns404() throws Exception {
        RewardDTO.UpdateRewardRequest updateRequest = RewardDTO.UpdateRewardRequest.builder()
                .name("Updated Reward")
                .description("Updated Description")
                .quantity(50)
                .probability(0.7)
                .maxQuantityInPeriod(5)
                .isActive(false)
                .build();

        mockMvc.perform(put("/api/v1/rewards/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRewardsByEvent_Returns200() throws Exception {
        // First create a reward
        String json = objectMapper.writeValueAsString(createRequest);
        mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // Then get rewards by event
        mockMvc.perform(get("/api/v1/rewards/event/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(createRequest.getName()))
                .andExpect(jsonPath("$[0].description").value(createRequest.getDescription()))
                .andExpect(jsonPath("$[0].quantity").value(createRequest.getQuantity()))
                .andExpect(jsonPath("$[0].probability").value(createRequest.getProbability()))
                .andExpect(jsonPath("$[0].maxQuantityInPeriod").value(createRequest.getMaxQuantityInPeriod()));
    }
}