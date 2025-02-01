package vn.com.fecredit.app.service;

import vn.com.fecredit.app.model.GoldenHourConfig;

import java.util.List;

public interface GoldenHourConfigService {
    List<GoldenHourConfig> getGoldenHourConfigs(String rewardName);
    // other method
}

