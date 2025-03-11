package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EntityScan(basePackages = {
    "vn.com.fecredit.app.entity",
    "vn.com.fecredit.app.entity.base"
})
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@EnableJpaAuditing
public class LuckyDrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckyDrawApplication.class, args);
    }
}
