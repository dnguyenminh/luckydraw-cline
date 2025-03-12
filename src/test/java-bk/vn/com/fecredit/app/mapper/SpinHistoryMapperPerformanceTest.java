package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("performance")
class SpinHistoryMapperPerformanceTest {

    private SpinHistoryMapper mapper;
    private List<SpinHistory> largeEntityList;
    private List<SpinResponse.CreateRequest> largeRequestList;
    private static final int LARGE_SIZE = 10_000;
    private static final int BULK_SIZE = 1_000;
    private static final int CONCURRENT_USERS = 100;

    @BeforeEach
    void setUp() {
        mapper = new SpinHistoryMapper();
        initializeTestData();
    }

    private void initializeTestData() {
        Event testEvent = createTestEvent();
        EventLocation testLocation = createTestLocation();
        Participant testParticipant = createTestParticipant();
        Reward testReward = createTestReward();

        largeEntityList = IntStream.range(0, LARGE_SIZE)
            .mapToObj(i -> createTestEntity(testEvent, testLocation, testParticipant, testReward, i))
            .collect(Collectors.toList());

        largeRequestList = IntStream.range(0, LARGE_SIZE)
            .mapToObj(this::createTestRequest)
            .collect(Collectors.toList());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void bulkResponseMapping_ShouldCompleteWithinTimeLimit() {
        List<SpinResponse.SpinResponse> responses = mapper.toResponseList(largeEntityList);
        
        assertNotNull(responses);
        assertEquals(LARGE_SIZE, responses.size());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void bulkEntityMapping_ShouldCompleteWithinTimeLimit() {
        List<SpinHistory> entities = largeRequestList.stream()
            .map(mapper::toEntity)
            .collect(Collectors.toList());
        
        assertNotNull(entities);
        assertEquals(LARGE_SIZE, entities.size());
    }

    @Test
    void parallelResponseMapping_ShouldBeThreadSafe() {
        List<SpinResponse.SpinResponse> responses = largeEntityList.parallelStream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());

        assertEquals(LARGE_SIZE, responses.size());
        assertThat(responses).doesNotContainNull();
    }

    @Test
    void parallelEntityMapping_ShouldBeThreadSafe() {
        List<SpinHistory> entities = largeRequestList.parallelStream()
            .map(mapper::toEntity)
            .collect(Collectors.toList());

        assertEquals(LARGE_SIZE, entities.size());
        assertThat(entities).doesNotContainNull();
    }

    @Test
    void statisticsCalculation_WithLargeDataset_ShouldBeEfficient() {
        long startTime = System.nanoTime();
        
        SpinResponse.Statistics stats = mapper.calculateStatistics(largeEntityList);
        
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        assertNotNull(stats);
        assertTrue(durationMs < 1000, "Statistics calculation took too long: " + durationMs + "ms");
    }

    @Test
    void concurrentUpdates_ShouldBeThreadSafe() {
        List<SpinHistory> entities = new ArrayList<>(CONCURRENT_USERS);
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            entities.add(new SpinHistory());
        }

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                SpinResponse.UpdateRequest request = createTestUpdateRequest(index);
                mapper.updateEntity(entities.get(index), request);
            });
            threads.add(thread);
            thread.start();
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread interrupted: " + e.getMessage());
            }
        });

        assertThat(entities)
            .hasSize(CONCURRENT_USERS)
            .allSatisfy(entity -> assertNotNull(entity.getSpinResult()));
    }

    private Event createTestEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        return event;
    }

    private EventLocation createTestLocation() {
        EventLocation location = new EventLocation();
        location.setId(1L);
        location.setName("Test Location");
        return location;
    }

    private Participant createTestParticipant() {
        Participant participant = new Participant();
        participant.setId(1L);
        return participant;
    }

    private Reward createTestReward() {
        Reward reward = new Reward();
        reward.setId(1L);
        reward.setName("Test Reward");
        return reward;
    }

    private SpinHistory createTestEntity(Event event, EventLocation location, 
                                       Participant participant, Reward reward, int index) {
        SpinHistory entity = new SpinHistory();
        entity.setId((long) index);
        entity.setEvent(event);
        entity.setLocation(location);
        entity.setParticipant(participant);
        entity.setReward(reward);
        entity.setSpinTime(LocalDateTime.now());
        entity.setWin(index % 2 == 0);
        entity.setSpinResult("Result " + index);
        entity.setWinProbability(0.7);
        entity.setFinalProbability(0.8);
        entity.setProbabilityMultiplier(1.2);
        entity.setGoldenHourActive(index % 3 == 0);
        return entity;
    }

    private SpinResponse.CreateRequest createTestRequest(int index) {
        return SpinResponse.CreateRequest.builder()
            .spinTime(LocalDateTime.now())
            .win(index % 2 == 0)
            .spinResult("Result " + index)
            .coordinates(10.0 + index, 20.0 + index)
            .deviceId("device" + index)
            .sessionId("session" + index)
            .probabilities(0.7, 0.8, 1.2)
            .goldenHour(index % 3 == 0, 1.5)
            .notes("Test notes " + index)
            .build();
    }

    private SpinResponse.UpdateRequest createTestUpdateRequest(int index) {
        return SpinResponse.UpdateRequest.builder()
            .spinResult("Updated Result " + index)
            .coordinates(15.0 + index, 25.0 + index)
            .deviceId("newDevice" + index)
            .sessionId("newSession" + index)
            .probabilities(0.8, 0.9, 1.3)
            .notes("Updated notes " + index)
            .build();
    }
}
