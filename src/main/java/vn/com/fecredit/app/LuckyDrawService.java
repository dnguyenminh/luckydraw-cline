package vn.com.fecredit.app;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


@Service
public class LuckyDrawService {

    private final ParticipantRepository participantRepository;
    private final RewardService rewardService;
    private final SpinHistoryRepository spinHistoryRepository;

    public LuckyDrawService(ParticipantRepository participantRepository, RewardService rewardService, SpinHistoryRepository spinHistoryRepository) {
        this.participantRepository = participantRepository;
        this.rewardService = rewardService;
        this.spinHistoryRepository = spinHistoryRepository;
    }

    @Transactional
    public List<SpinResult> drawWinners(int numWinners) throws NoParticipantsException {


        long totalParticipants = participantRepository.count();
        if (totalParticipants == 0) {
            throw new NoParticipantsException("Không có người tham gia nào.");
        }
        int numToDraw = Math.min(numWinners, (int) totalParticipants);


        List<SpinResult> spinResults = new ArrayList<>();
        Random random = new Random();


        for (int i = 0; i < numToDraw; i++) {
            int randomPage = random.nextInt((int) (totalParticipants / numWinners + (totalParticipants % numWinners == 0 ? 0 : 1)));
            int randomOffset = (int) (random.nextInt((int) Math.min(numWinners, totalParticipants - ((long) randomPage * numWinners))));
            PageRequest pageRequest = PageRequest.of(randomPage, numWinners);
            Participant winner = participantRepository.findAll(pageRequest).getContent().get(randomOffset);
            SpinResult spinResult = rewardService.determineWinningReward(winner.getCustomerId());

            if (spinResult.isWinner()) {
                spinResults.add(spinResult);
            }


        }
        return spinResults;
    }

    public void createParticipant(String customerId) {
        Participant participant = new Participant(customerId);
        participantRepository.save(participant);
    }
}