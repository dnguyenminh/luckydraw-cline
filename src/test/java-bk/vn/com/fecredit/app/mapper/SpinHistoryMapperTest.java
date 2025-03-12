package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SpinHistoryMapperTest {

    private SpinHistoryMapper mapper;

    @Mock private EventRepository eventRepository;
    @Mock private EventLocationRepository locationRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private RewardRepository rewardRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new SpinHistoryMapperImpl();
        mockRepositories();
    }

    private void mockRepositories() {
        when(eventRepository.findById(any())).thenReturn(Optional.of(new Event()));
        when(locationRepository.findById(any())).thenReturn(Optional.of(new EventLocation()));
        when(participantRepository.findById(any())).thenReturn(Optional.of(new Participant()));
        when(rewardRepository.findById(any())).thenReturn(Optional.of(new Reward()));
    }

    @Test
    void shouldMapToEntity() {
        SpinHistoryDTO.CreateRequest request = SpinHistoryDTO.CreateRequest.builder()
            .eventId(1L)
            .locationId(2L)
            .rewardId(3L)
            .participantId(4L)
            .build();

        SpinHistory result = mapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isNotNull();
        assertThat(result.getLocation()).isNotNull();
        assertThat(result.getReward()).isNotNull();
        assertThat(result.getParticipant()).isNotNull();
    }

    @Test
    void shouldMapToResponse() {
        SpinHistory entity = new SpinHistory();
        setupEntityRelations(entity);

        SpinHistoryDTO.Response response = mapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertBasicMapping(response);
    }

    @Test
    void shouldHandleNullEntity() {
        SpinHistoryDTO.Response response = mapper.toResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void shouldMapToSummary() {
        SpinHistory entity = new SpinHistory();
        setupEntityRelations(entity);

        SpinHistoryDTO.Summary summary = mapper.toSummary(entity);

        assertThat(summary).isNotNull();
        assertBasicSummaryMapping(summary);
    }

    @Test
    void shouldMapEntitySet() {
        Set<SpinHistory> entities = new HashSet<>();
        SpinHistory entity = new SpinHistory();
        setupEntityRelations(entity);
        entities.add(entity);

        Set<SpinHistoryDTO.Response> responses = mapper.toResponseSet(entities);

        assertThat(responses).hasSize(1);
        assertBasicMapping(responses.iterator().next());
    }

    @Test
    void shouldHandleEmptySet() {
        Set<SpinHistoryDTO.Response> responses = mapper.toResponseSet(new HashSet<>());
        assertThat(responses).isEmpty();
    }

    @Test
    void shouldHandleNullSet() {
        Set<SpinHistoryDTO.Response> responses = mapper.toResponseSet(null);
        assertThat(responses).isEmpty();
    }

    @Test
    void shouldCreateFromRequest() {
        SpinHistoryDTO.CreateRequest request = SpinHistoryDTO.CreateRequest.builder()
            .eventId(1L)
            .locationId(2L)
            .rewardId(3L)
            .participantId(4L)
            .build();

        SpinHistoryDTO.Response response = mapper.createFromRequest(request);

        assertThat(response).isNotNull();
        assertBasicMapping(response);
    }

    private void setupEntityRelations(SpinHistory entity) {
        Event event = new Event();
        event.setName("Test Event");
        
        EventLocation location = new EventLocation();
        location.setName("Test Location");
        
        Reward reward = new Reward();
        reward.setName("Test Reward");
        
        Participant participant = new Participant();
        participant.setCustomerId("Test User");

        entity.setEvent(event);
        entity.setLocation(location);
        entity.setReward(reward);
        entity.setParticipant(participant);
    }

    private void assertBasicMapping(SpinHistoryDTO.Response response) {
        assertThat(response.getEventName()).isEqualTo("Test Event");
        assertThat(response.getLocationName()).isEqualTo("Test Location");
        assertThat(response.getRewardName()).isEqualTo("Test Reward");
        assertThat(response.getParticipantName()).isEqualTo("Test User");
    }

    private void assertBasicSummaryMapping(SpinHistoryDTO.Summary summary) {
        assertThat(summary.getLocationName()).isEqualTo("Test Location");
        assertThat(summary.getRewardName()).isEqualTo("Test Reward");
        assertThat(summary.getParticipantName()).isEqualTo("Test User");
    }
}
