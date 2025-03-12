package vn.com.fecredit.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@EntityScan(basePackages = "vn.com.fecredit.app.entity")
@ActiveProfiles("test")
public class TestJpaConfig {
    // Configuration for test environment
}
