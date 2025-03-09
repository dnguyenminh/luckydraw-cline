package vn.com.fecredit.app;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
//@EntityScan(basePackages = {
//    "vn.com.fecredit.app.entity",
//    "vn.com.fecredit.app.entity.base"
//})
@EnableTransactionManagement
public class TestJpaConfig {

    // Additional test-specific JPA configuration can be added here
    // For example:
    // @Bean
    // public PlatformTransactionManager transactionManager() {
    //     return new JpaTransactionManager();
    // }
}
