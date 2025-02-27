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
import vn.com.fecredit.app.dto.golden.UpdateGoldenHourRequest;
import vn.com.fecredit.app.service.GoldenHourService;

@WebMvcTest(GoldenHourController.class)
class GoldenHourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoldenHourService goldenHourService;

    private CreateGoldenHourRequest createRequest;
    private UpdateGoldenHourRequest updateRequest;
    private GoldenHourDTO testDTO;

    @BeforeEach
    void setUp() {
        createRequest = CreateGoldenHourRequest.builder()
                .eventId(1L)
                .name("Test Golden Hour")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .multiplier(2.0)
                .build();

        updateRequest = UpdateGoldenHourRequest.builder()
                .name("Updated Golden Hour")
                .multiplier(3.0)
                .build();

        testDTO = GoldenHourDTO.builder()
                .id(1L)
                .eventId(1L)
                .name("Test Golden Hour")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_Success() throws Exception {
        when(goldenHourService.create(any(CreateGoldenHourRequest.class)))
                .thenReturn(testDTO);

        mockMvc.perform(post("/api/v1/golden-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Golden Hour"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_Success() throws Exception {
        when(goldenHourService.update(anyLong(), any(UpdateGoldenHourRequest.class)))
                .thenReturn(testDTO);

        mockMvc.perform(put("/api/v1/golden-hours/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllByEventId_Success() throws Exception {
        when(goldenHourService.getAllByEventId(anyLong()))
                .thenReturn(Arrays.asList(testDTO));

        mockMvc.perform(get("/api/v1/golden-hours/event/{eventId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Golden Hour"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCurrentGoldenHour_Success() throws Exception {
        when(goldenHourService.getCurrentGoldenHour(anyLong()))
                .thenReturn(Optional.of(testDTO));

        mockMvc.perform(get("/api/v1/golden-hours/current/event/{eventId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCurrentGoldenHour_NotFound() throws Exception {
        when(goldenHourService.getCurrentGoldenHour(anyLong()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/golden-hours/current/event/{eventId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/golden-hours/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/golden-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}