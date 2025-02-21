package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.model.*;
import vn.com.fecredit.app.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpinServiceConcurrencyTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private SpinHistoryRepository spinHistoryRepository;
    @Mock
    private LuckyDrawResultRepository luckyDrawResultRepository;
    @Mock
    private GoldenHourRepository goldenHourRepository;
    @Mock
    private RewardSelectionService rewardSelectionService;

    private SpinService spinService;
    private Event event;
    private List<Participant> participants;
    private List<Reward> rewards;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        spinService = new SpinService(
                eventRepository,
                participantRepository,
                rewardRepository,
                spinHistoryRepository,
                luckyDrawResultRepository,
                goldenHourRepository,
                rewardSelectionService
        );

        now = LocalDateTime.now();
        event = Event.builder()
                .id(1L)
                .code("TEST001")
                .name("Test Event")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(1))
                .totalSpins(1000L)
                .remainingSpins(1000L)
                .isActive(true)
                .build();

        // Tạo test rewards
        rewards = Arrays.asList(
                Reward.builder()
                        .id(1L)
                        .name("Reward 1")
                        .quantity(50)
                        .remainingQuantity(50)
                        .isActive(true)
                        .build(),
                Reward.builder()
                        .id(2L)
                        .name("Reward 2")
                        .quantity(100)
                        .remainingQuantity(100)
                        .isActive(true)
                        .build()
        );

        // Tạo test participants
        participants = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            participants.add(Participant.builder()
                    .id((long) i + 1)
                    .event(event)
                    .isActive(true)
                    .build());
        }

        // Setup mock trả về kết quả
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rewardRepository.findActiveRewardsByEventId(1L)).thenReturn(rewards);
        when(goldenHourRepository.findActiveGoldenHour(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        for (Participant p : participants) {
            when(participantRepository.findById(p.getId())).thenReturn(Optional.of(p));
        }

        // Mock lưu spin history
        when(spinHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock lưu lucky draw result
        when(luckyDrawResultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldHandleConcurrentSpinsCorrectly() throws InterruptedException {
        int numberOfThreads = participants.size(); // 10
        int spinsPerThread = 20;
        AtomicInteger spinCount = new AtomicInteger();

        // Setup reward selection: luân phiên trả về reward hoặc không có reward
        when(rewardSelectionService.selectReward(
                any(Event.class),
                anyList(),
                anyLong(),
                any(),
                anyString()))
                .thenAnswer(invocation -> {
                    int count = spinCount.getAndIncrement();
                    if (count % 3 == 0) {
                        return Optional.of(rewards.get(0));
                    } else if (count % 3 == 1) {
                        return Optional.of(rewards.get(1));
                    } else {
                        return Optional.empty();
                    }
                });

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);

        ConcurrentHashMap<Long, AtomicInteger> participantSpinCounts = initParticipantSpinCounts(participants);
        ConcurrentHashMap<Long, AtomicInteger> rewardWinCounts = initRewardWinCounts(rewards);
        Set<Long> remainingSpinsValues = ConcurrentHashMap.newKeySet();

        List<Future<List<SpinHistory>>> futures = createParticipantTasks(
                spinsPerThread, startLatch, completionLatch,
                participantSpinCounts, rewardWinCounts, remainingSpinsValues
        );

        // Bắt đầu chạy các task cùng lúc
        startLatch.countDown();
        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Các kiểm tra (assert)
        assertThat(completed).isTrue();
        verifyParticipantSpinCounts(participantSpinCounts, spinsPerThread);
        int totalSpins = numberOfThreads * spinsPerThread;
        assertThat(spinCount.get()).isEqualTo(totalSpins);
        verifyRemainingSpinsSequential(remainingSpinsValues);
        verifyRewardDistribution(rewardWinCounts, totalSpins, rewards);
    }

    // --- Các phương thức helper ---

    /**
     * Tạo map đếm số lần quay cho mỗi participant
     */
    private ConcurrentHashMap<Long, AtomicInteger> initParticipantSpinCounts(List<Participant> participants) {
        ConcurrentHashMap<Long, AtomicInteger> map = new ConcurrentHashMap<>();
        participants.forEach(p -> map.put(p.getId(), new AtomicInteger()));
        return map;
    }

    /**
     * Tạo map đếm số lần thắng cho mỗi reward
     */
    private ConcurrentHashMap<Long, AtomicInteger> initRewardWinCounts(List<Reward> rewards) {
        ConcurrentHashMap<Long, AtomicInteger> map = new ConcurrentHashMap<>();
        rewards.forEach(r -> map.put(r.getId(), new AtomicInteger()));
        return map;
    }

    /**
     * Tạo danh sách các task cho từng participant.
     */
    private List<Future<List<SpinHistory>>> createParticipantTasks(
            int spinsPerThread,
            CountDownLatch startLatch,
            CountDownLatch completionLatch,
            ConcurrentHashMap<Long, AtomicInteger> participantSpinCounts,
            ConcurrentHashMap<Long, AtomicInteger> rewardWinCounts,
            Set<Long> remainingSpinsValues
    ) {
        List<Future<List<SpinHistory>>> futures = new ArrayList<>();
        // Với mỗi participant, tạo một task riêng
        for (Participant participant : participants) {
            Callable<List<SpinHistory>> task = () -> runSpinsForParticipant(
                    participant, spinsPerThread, startLatch,
                    participantSpinCounts, rewardWinCounts, remainingSpinsValues, completionLatch
            );
            futures.add(Executors.newSingleThreadExecutor().submit(task));
        }
        return futures;
    }

    /**
     * Task của một participant: thực hiện nhiều lượt quay.
     */
    private List<SpinHistory> runSpinsForParticipant(
            Participant participant,
            int spinsPerThread,
            CountDownLatch startLatch,
            ConcurrentHashMap<Long, AtomicInteger> participantSpinCounts,
            ConcurrentHashMap<Long, AtomicInteger> rewardWinCounts,
            Set<Long> remainingSpinsValues,
            CountDownLatch completionLatch
    ) throws InterruptedException {
        List<SpinHistory> histories = new ArrayList<>();
        startLatch.await(); // Đợi đến khi tất cả các thread bắt đầu
        for (int i = 0; i < spinsPerThread; i++) {
            SpinRequest request = SpinRequest.builder()
                    .eventId(event.getId())
                    .participantId(participant.getId())
                    .customerLocation("Location1")
                    .isGoldenHourEligible(true)
                    .hasActiveParticipation(true)
                    .remainingSpinsForParticipant(3L)
                    .participantStatus("ACTIVE")
                    .build();

            SpinHistory history = spinService.spin(request);
            histories.add(history);
            participantSpinCounts.get(participant.getId()).incrementAndGet();
            if (history.getReward() != null) {
                rewardWinCounts.get(history.getReward().getId()).incrementAndGet();
            }
            remainingSpinsValues.add(history.getRemainingSpins());
        }
        completionLatch.countDown();
        return histories;
    }

    /**
     * Kiểm tra số lượt quay của từng participant có đúng không.
     */
    private void verifyParticipantSpinCounts(Map<Long, AtomicInteger> participantSpinCounts, int spinsPerThread) {
        participantSpinCounts.forEach((participantId, count) ->
                assertThat(count.get()).isEqualTo(spinsPerThread)
        );
    }

    /**
     * Kiểm tra xem các giá trị remaining spins có giảm liên tục không.
     */
    private void verifyRemainingSpinsSequential(Set<Long> remainingSpinsValues) {
        List<Long> sortedRemainingSpins = new ArrayList<>(remainingSpinsValues);
        Collections.sort(sortedRemainingSpins);
        for (int i = 0; i < sortedRemainingSpins.size() - 1; i++) {
            assertThat(sortedRemainingSpins.get(i + 1) - sortedRemainingSpins.get(i)).isEqualTo(1);
        }
    }

    /**
     * Kiểm tra phân phối reward hợp lý.
     */
    private void verifyRewardDistribution(Map<Long, AtomicInteger> rewardWinCounts, int totalSpins, List<Reward> rewards) {
        int expectedWinsPerReward = totalSpins / 3;
        rewards.forEach(reward -> {
            int wins = rewardWinCounts.get(reward.getId()).get();
            // Cho phép lệch 1 lượt
            assertThat(wins).isLessThanOrEqualTo(expectedWinsPerReward + 1);
        });
    }
}
