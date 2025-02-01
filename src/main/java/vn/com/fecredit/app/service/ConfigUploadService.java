package vn.com.fecredit.app.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fecredit.app.model.GoldenHourConfig;
import vn.com.fecredit.app.reposistory.GoldenHourConfigRepository;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.reposistory.RewardRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigUploadService {
    private final RewardRepository rewardRepository;
    private final GoldenHourConfigRepository goldenHourConfigRepository;

    public ConfigUploadService(RewardRepository rewardRepository, GoldenHourConfigRepository goldenHourConfigRepository) {
        this.rewardRepository = rewardRepository;
        this.goldenHourConfigRepository = goldenHourConfigRepository;
    }

    // Xử lý upload file phần thưởng
    public void uploadRewardConfig(MultipartFile file) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<Reward> csvToBean = new CsvToBeanBuilder<Reward>(reader)
                    .withType(Reward.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false) // Tắt ném exception tự động
                    .build();

            List<Reward> rewards = csvToBean.parse();

            // Kiểm tra lỗi parsing
            List<CsvException> exceptions = csvToBean.getCapturedExceptions();
            if (!exceptions.isEmpty()) {
                String errorMessage = exceptions.stream()
                        .map(ex -> "Lỗi ở dòng " + ex.getLineNumber() + ": " + ex.getMessage())
                        .collect(Collectors.joining("\n"));
                throw new RuntimeException(errorMessage);
            }

            rewardRepository.saveAll(rewards);
        }
    }

    // Xử lý upload file giờ vàng
    public void uploadGoldenHourConfig(MultipartFile file) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<GoldenHourConfig> csvToBean = new CsvToBeanBuilder<GoldenHourConfig>(reader)
                    .withType(GoldenHourConfig.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<GoldenHourConfig> goldenHourConfigs = csvToBean.parse();
            goldenHourConfigRepository.saveAll(goldenHourConfigs);
        }
    }
}