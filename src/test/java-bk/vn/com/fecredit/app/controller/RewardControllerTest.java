package vn.com.fecredit.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.service.RewardService;
import vn.com.fecredit.app.exception.GlobalExceptionHandler;
import vn.com.fecredit.app.exception.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RewardControllerTest {

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private RewardController rewardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(rewardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReward_Success() throws Exception {
        RewardDTO.Response response = RewardDTO.Response.builder()
                .id(1L)
                .name("Test Reward")
                .build();
        
        when(rewardService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Reward\",\"quantity\":10,\"eventId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Reward"));
    }

    @Test
    void createReward_ValidationFailure() throws Exception {
        mockMvc.perform(post("/api/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReward_Success() throws Exception {
        RewardDTO.Response response = RewardDTO.Response.builder()
                .id(1L)
                .name("Updated Reward")
                .build();

        when(rewardService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/rewards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Reward\",\"quantity\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Reward"));
    }

    @Test
    void updateReward_NotFound() throws Exception {
        when(rewardService.update(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Reward not found"));

        mockMvc.perform(put("/api/rewards/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Reward\",\"quantity\":20}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRewards_Success() throws Exception {
        List<RewardDTO.Response> rewards = Arrays.asList(
            RewardDTO.Response.builder().id(1L).name("Reward 1").build(),
            RewardDTO.Response.builder().id(2L).name("Reward 2").build()
        );
        Page<RewardDTO.Response> page = new PageImpl<>(rewards);
        
        when(rewardService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Reward 1"))
                .andExpect(jsonPath("$.content[1].name").value("Reward 2"));
    }

    @Test
    void getActiveRewardsByEvent_Success() throws Exception {
        List<RewardDTO.Summary> summaries = Arrays.asList(
            RewardDTO.Summary.builder().id(1L).name("Reward 1").build(),
            RewardDTO.Summary.builder().id(2L).name("Reward 2").build()
        );
        
        when(rewardService.findAllActiveByEventId(1L)).thenReturn(summaries);

        mockMvc.perform(get("/api/rewards/event/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Reward 1"))
                .andExpect(jsonPath("$[1].name").value("Reward 2"));
    }

    @Test
    void updateQuantity_Success() throws Exception {
        mockMvc.perform(put("/api/rewards/1/quantity")
                .param("quantity", "50"))
                .andExpect(status().isOk());

        verify(rewardService).updateRemainingQuantity(1L, 50);
    }

    @Test
    void updateQuantity_InvalidQuantity() throws Exception {
        mockMvc.perform(put("/api/rewards/1/quantity")
                .param("quantity", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReward_Success() throws Exception {
        mockMvc.perform(delete("/api/rewards/1"))
                .andExpect(status().isNoContent());

        verify(rewardService).delete(1L);
    }

    @Test
    void deleteReward_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Reward not found"))
                .when(rewardService).delete(999L);

        mockMvc.perform(delete("/api/rewards/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReward_Success() throws Exception {
        RewardDTO.Response response = RewardDTO.Response.builder()
                .id(1L)
                .name("Test Reward")
                .build();

        when(rewardService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Reward"));
    }

    @Test
    void getReward_NotFound() throws Exception {
        when(rewardService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Reward not found"));

        mockMvc.perform(get("/api/rewards/999"))
                .andExpect(status().isNotFound());
    }
}
