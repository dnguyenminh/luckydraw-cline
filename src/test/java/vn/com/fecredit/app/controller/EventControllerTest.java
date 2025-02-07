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
import vn.com.fecredit.app.dto.EventDTO;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EventDTO.CreateEventRequest createRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        createRequest = EventDTO.CreateEventRequest.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .description("Test Event Description")
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(7))
                .build();
    }

    @Test
    void createEvent_ValidInput_Returns201() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createRequest.getName()))
                .andExpect(jsonPath("$.code").value(createRequest.getCode()))
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()));
    }

    @Test
    void createEvent_InvalidInput_Returns400() throws Exception {
        createRequest.setName("");
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createEvent_WithUserRole_Returns403() throws Exception {
        String json = objectMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEvent_ExistingId_Returns200() throws Exception {
        // First create an event
        String json = objectMapper.writeValueAsString(createRequest);
        String responseContent = mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString();

        // Extract id from response
        Long eventId = objectMapper.readTree(responseContent).get("id").asLong();

        // Then get the event
        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(createRequest.getName()))
                .andExpect(jsonPath("$.code").value(createRequest.getCode()))
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()));
    }

    @Test
    void getEvent_NonExistingId_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/events/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_ExistingId_Returns200() throws Exception {
        // First create an event
        String json = objectMapper.writeValueAsString(createRequest);
        String responseContent = mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse().getContentAsString();

        // Extract id from response
        Long eventId = objectMapper.readTree(responseContent).get("id").asLong();

        // Update request
        EventDTO.UpdateEventRequest updateRequest = EventDTO.UpdateEventRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .build();

        // Then update the event
        mockMvc.perform(put("/api/v1/events/{id}", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.description").value(updateRequest.getDescription()));
    }

    @Test
    void updateEvent_NonExistingId_Returns404() throws Exception {
        EventDTO.UpdateEventRequest updateRequest = EventDTO.UpdateEventRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .build();

        mockMvc.perform(put("/api/v1/events/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }
}