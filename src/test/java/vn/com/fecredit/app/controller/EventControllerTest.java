package vn.com.fecredit.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.service.EventService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the EventController.
 * Tests various scenarios including successful operations and error cases.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Event Controller Tests")
@WithMockUser(roles = "ADMIN")
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private EventDTO.CreateEventRequest createRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        createRequest = EventDTO.CreateEventRequest.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .description("Test Event Description")
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Create Event Tests")
    @WithMockUser(roles = "ADMIN")
    class CreateEventTests {
        
        @Test
        @DisplayName("Should create event when input is valid")
        void shouldCreateEventWhenValidInput() throws Exception {
            EventDTO expectedResponse = createExpectedEventDTO(1L, createRequest);
            when(eventService.createEvent(any(EventDTO.CreateEventRequest.class)))
                    .thenReturn(expectedResponse);

            mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value(createRequest.getName()))
                    .andExpect(jsonPath("$.code").value(createRequest.getCode()))
                    .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                    .andExpect(jsonPath("$.startDate").exists())
                    .andExpect(jsonPath("$.endDate").exists())
                    .andExpect(jsonPath("$.isActive").value(createRequest.getIsActive()));

            verify(eventService).createEvent(any(EventDTO.CreateEventRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is empty")
        void shouldReturn400WhenNameIsEmpty() throws Exception {
            createRequest.setName("");
            
            mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        @DisplayName("Should return 400 when dates are invalid")
        void shouldReturn400WhenDatesAreInvalid() throws Exception {
            createRequest.setStartDate(now.plusDays(7));
            createRequest.setEndDate(now.plusDays(1));

            when(eventService.createEvent(any(EventDTO.CreateEventRequest.class)))
                    .thenThrow(new IllegalArgumentException("Event start date must be before end date"));

            mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 400 when code is invalid")
        void shouldReturn400WhenCodeIsInvalid() throws Exception {
            createRequest.setCode("invalid code"); // Space not allowed
            
            mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.code").exists());
        }
    }

    @Nested
    @DisplayName("Get Event Tests")
    @WithMockUser(roles = "ADMIN")
    class GetEventTests {
        
        @Test
        @DisplayName("Should return event when ID exists")
        void shouldReturnEventWhenIdExists() throws Exception {
            Long eventId = 1L;
            EventDTO expectedEvent = createExpectedEventDTO(eventId, createRequest);
            when(eventService.getEvent(eventId)).thenReturn(expectedEvent);

            mockMvc.perform(get("/api/events/{id}", eventId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventId))
                    .andExpect(jsonPath("$.name").value(createRequest.getName()))
                    .andExpect(jsonPath("$.code").value(createRequest.getCode()))
                    .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                    .andExpect(jsonPath("$.startDate").exists())
                    .andExpect(jsonPath("$.endDate").exists())
                    .andExpect(jsonPath("$.isActive").value(true));

            verify(eventService).getEvent(eventId);
        }

        @Test
        @DisplayName("Should return 404 when ID does not exist")
        void shouldReturn404WhenIdDoesNotExist() throws Exception {
            Long nonExistingId = 999L;
            when(eventService.getEvent(nonExistingId))
                    .thenThrow(new ResourceNotFoundException("Event", "id", nonExistingId));

            mockMvc.perform(get("/api/events/{id}", nonExistingId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Resource Not Found"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").exists());
        }
    }

    @Nested
    @DisplayName("Update Event Tests")
    @WithMockUser(roles = "ADMIN")
    class UpdateEventTests {
        
        @Test
        @DisplayName("Should update event when input is valid")
        void shouldUpdateEventWhenValidInput() throws Exception {
            Long eventId = 1L;
            EventDTO.UpdateEventRequest updateRequest = EventDTO.UpdateEventRequest.builder()
                    .name("Updated Event")
                    .description("Updated Description")
                    .startDate(now.plusDays(2))
                    .endDate(now.plusDays(8))
                    .isActive(false)
                    .build();

            EventDTO updatedEvent = EventDTO.builder()
                    .id(eventId)
                    .name(updateRequest.getName())
                    .description(updateRequest.getDescription())
                    .startDate(updateRequest.getStartDate())
                    .endDate(updateRequest.getEndDate())
                    .isActive(updateRequest.getIsActive())
                    .build();

            when(eventService.updateEvent(eq(eventId), any(EventDTO.UpdateEventRequest.class)))
                    .thenReturn(updatedEvent);

            mockMvc.perform(put("/api/events/{id}", eventId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventId))
                    .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                    .andExpect(jsonPath("$.description").value(updateRequest.getDescription()))
                    .andExpect(jsonPath("$.startDate").exists())
                    .andExpect(jsonPath("$.endDate").exists())
                    .andExpect(jsonPath("$.isActive").value(updateRequest.getIsActive()));

            verify(eventService).updateEvent(eq(eventId), any(EventDTO.UpdateEventRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existing event")
        void shouldReturn404WhenUpdatingNonExistingEvent() throws Exception {
            Long nonExistingId = 999L;
            EventDTO.UpdateEventRequest updateRequest = createSimpleUpdateRequest();

            when(eventService.updateEvent(eq(nonExistingId), any(EventDTO.UpdateEventRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Event", "id", nonExistingId));

            mockMvc.perform(put("/api/events/{id}", nonExistingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Resource Not Found"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").exists());
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 when user has insufficient privileges")
    void shouldReturn403WhenUserHasInsufficientPrivileges() throws Exception {
        when(eventService.createEvent(any(EventDTO.CreateEventRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event", "id", 1L));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.path").exists());
    }

    private EventDTO createExpectedEventDTO(Long id, EventDTO.CreateEventRequest request) {
        return EventDTO.builder()
                .id(id)
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive())
                .build();
    }

    private EventDTO.UpdateEventRequest createSimpleUpdateRequest() {
        return EventDTO.UpdateEventRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .build();
    }
}