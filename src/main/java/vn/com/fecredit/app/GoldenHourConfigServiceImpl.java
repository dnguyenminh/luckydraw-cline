package vn.com.fecredit.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoldenHourConfigServiceImpl implements GoldenHourConfigService {

    @Autowired
    private GoldenHourConfigRepository goldenHourConfigRepository;

    @Override
    public List<GoldenHourConfig> getGoldenHourConfigs(String rewardName) {
        return goldenHourConfigRepository.findByRewardName(rewardName);
    }
}