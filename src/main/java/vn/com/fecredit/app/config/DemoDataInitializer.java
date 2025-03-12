package vn.com.fecredit.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter SQL_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TEMPLATE_LOCATION = "db/demo/demo-data-template.sql";
    
    @Override
    @Transactional
    public void run(String... args) {
        try {
            var resource = new ClassPathResource(TEMPLATE_LOCATION);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            LocalDateTime now = LocalDateTime.now();
            String sql = template
                // Past timestamps
                .replace("{PAST_3Y}", now.minusYears(3).format(SQL_TIMESTAMP))
                .replace("{PAST_2Y}", now.minusYears(2).format(SQL_TIMESTAMP))
                .replace("{PAST_1Y}", now.minusYears(1).format(SQL_TIMESTAMP))
                .replace("{PAST_6M}", now.minusMonths(6).format(SQL_TIMESTAMP))
                .replace("{PAST_3M}", now.minusMonths(3).format(SQL_TIMESTAMP))
                .replace("{PAST_1M}", now.minusMonths(1).format(SQL_TIMESTAMP))
                
                // Current timestamp
                .replace("{NOW}", now.format(SQL_TIMESTAMP))
                
                // Future timestamps
                .replace("{FUTURE_1M}", now.plusMonths(1).format(SQL_TIMESTAMP))
                .replace("{FUTURE_3M}", now.plusMonths(3).format(SQL_TIMESTAMP))
                .replace("{FUTURE_6M}", now.plusMonths(6).format(SQL_TIMESTAMP))
                .replace("{FUTURE_1Y}", now.plusYears(1).format(SQL_TIMESTAMP))
                .replace("{FUTURE_2Y}", now.plusYears(2).format(SQL_TIMESTAMP));
            
            // Execute the prepared SQL
            log.info("Loading demo data...");
            jdbcTemplate.execute(sql);
            log.info("Demo data loaded successfully");
            
        } catch (Exception e) {
            log.error("Failed to initialize demo data", e);
            throw new RuntimeException("Failed to initialize demo data", e);
        }
    }
}
