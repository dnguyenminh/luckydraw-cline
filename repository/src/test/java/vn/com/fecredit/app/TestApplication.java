package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
//@EntityScan(basePackages = {
//    "vn.com.fecredit.app.entity",
//    "vn.com.fecredit.app.entity.base"
//})
// Removed duplicate @EnableJpaRepositories annotation as it's already defined in TestConfig
//@ComponentScan(basePackages = {
//    "vn.com.fecredit.app.repository",
//    "vn.com.fecredit.app.repository.test"
//})
@Import(TestConfig.class)
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
