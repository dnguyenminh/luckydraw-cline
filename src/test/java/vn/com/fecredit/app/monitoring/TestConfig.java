package vn.com.fecredit.app.monitoring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import vn.com.fecredit.app.config.SecurityConfig;
import vn.com.fecredit.app.monitoring.EventStatisticsMonitor;

@Configuration
@TestPropertySource(locations = "classpath:application-test.yml")
@Import(SecurityConfig.class)
public class TestConfig {

    @Bean
    public EventStatisticsMonitor eventStatisticsMonitor() {
        return EventStatisticsMonitor.getInstance();
    }
}
