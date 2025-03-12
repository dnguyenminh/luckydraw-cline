package vn.com.fecredit.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@EntityScan(basePackages = {
    "vn.com.fecredit.app.entity",
    "vn.com.fecredit.app.entity.base"
})
public class JpaConfig {
    // JPA configuration methods can be added here if needed
    // For example: custom EntityManager configuration, transaction management, etc.
}
