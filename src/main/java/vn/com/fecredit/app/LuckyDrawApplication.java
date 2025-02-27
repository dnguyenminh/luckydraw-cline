package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories("vn.com.fecredit.app.repository")
@EntityScan("vn.com.fecredit.app.entity")
@EnableTransactionManagement
@EnableScheduling
public class LuckyDrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckyDrawApplication.class, args);
    }
}
