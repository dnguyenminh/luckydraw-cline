package vn.com.fecredit.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.impl.RewardServiceImpl;

@TestConfiguration
public class TestConfig {

    @MockBean
    private RewardRepository rewardRepository;

    @MockBean
    private EventLocationRepository eventLocationRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private RewardMapper rewardMapper;

    @Bean
    @Primary
    public RewardServiceImpl rewardService() {
        return new RewardServiceImpl(rewardRepository, eventLocationRepository, eventRepository, rewardMapper);
    }
}
