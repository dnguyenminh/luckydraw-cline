package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
//@EntityScan(basePackages = {
//    "vn.com.fecredit.app.entity",
//    "vn.com.fecredit.app.entity.base"
//})
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
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
