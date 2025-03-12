package vn.com.fecredit.app.mapper;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@State(Scope.Thread)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xms2G", "-Xmx2G"})
public class SpinHistoryMapperBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        SpinHistoryMapper mapper;
        SpinHistory testEntity;
        SpinHistoryDTO.CreateRequest testRequest;
        List<SpinHistory> entityList;
        List<SpinHistoryDTO.CreateRequest> requestList;
        static final int LIST_SIZE = 1000;

        @Setup(Level.Trial)
        public void setup() {
            mapper = new SpinHistoryMapper();
            setupTestData();
        }

        private void setupTestData() {
            Event event = new Event();
            event.setId(1L);
            event.setName("Test Event");

            EventLocation location = new EventLocation();
            location.setId(2L);
            location.setName("Test Location");

            Participant participant = new Participant();
            participant.setId(3L);

            Reward reward = new Reward();
            reward.setId(4L);
            reward.setName("Test Reward");

            testEntity = createTestEntity(event, location, participant, reward, 1);
            testRequest = createTestRequest(1);
            entityList = createEntityList(event, location, participant, reward);
            requestList = createRequestList();
        }

        private List<SpinHistory> createEntityList(Event event, EventLocation location, 
                                                 Participant participant, Reward reward) {
            return IntStream.range(0, LIST_SIZE)
                .mapToObj(i -> createTestEntity(event, location, participant, reward, i))
                .collect(Collectors.toList());
        }

        private List<SpinHistoryDTO.CreateRequest> createRequestList() {
            return IntStream.range(0, LIST_SIZE)
                .mapToObj(this::createTestRequest)
                .collect(Collectors.toList());
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
            entity.setLatitude(10.0 + index);
            entity.setLongitude(20.0 + index);
            entity.setDeviceId("device" + index);
            entity.setSessionId("session" + index);
            entity.setWinProbability(0.7);
            entity.setFinalProbability(0.8);
            entity.setProbabilityMultiplier(1.2);
            entity.setGoldenHourActive(index % 3 == 0);
            entity.setGoldenHourMultiplier(1.5);
            entity.setNotes("Test notes " + index);
            return entity;
        }

        private SpinHistoryDTO.CreateRequest createTestRequest(int index) {
            return SpinHistoryDTO.CreateRequest.builder()
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
    }

    @Benchmark
    @Group("SingleOperations")
    public void toResponse(BenchmarkState state, Blackhole blackhole) {
        SpinHistoryDTO.SpinResponse response = state.mapper.toResponse(state.testEntity);
        blackhole.consume(response);
    }

    @Benchmark
    @Group("SingleOperations")
    public void toEntity(BenchmarkState state, Blackhole blackhole) {
        SpinHistory entity = state.mapper.toEntity(state.testRequest);
        blackhole.consume(entity);
    }

    @Benchmark
    @Group("BulkOperations")
    public void toResponseList(BenchmarkState state, Blackhole blackhole) {
        List<SpinHistoryDTO.SpinResponse> responses = state.mapper.toResponseList(state.entityList);
        blackhole.consume(responses);
    }

    @Benchmark
    @Group("BulkOperations")
    public void parallelToResponseList(BenchmarkState state, Blackhole blackhole) {
        List<SpinHistoryDTO.SpinResponse> responses = state.entityList.parallelStream()
            .map(state.mapper::toResponse)
            .collect(Collectors.toList());
        blackhole.consume(responses);
    }

    @Benchmark
    @Group("Statistics")
    public void calculateStatistics(BenchmarkState state, Blackhole blackhole) {
        SpinHistoryDTO.Statistics stats = state.mapper.calculateStatistics(state.entityList);
        blackhole.consume(stats);
    }
}
