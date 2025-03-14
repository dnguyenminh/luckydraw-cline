package vn.com.fecredit.app;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@AutoConfigureDataJpa
@EntityScan(basePackages = {
    "vn.com.fecredit.app.entity",
    "vn.com.fecredit.app.entity.base"
})
@EnableJpaRepositories(
    basePackages = "vn.com.fecredit.app.repository",
    considerNestedRepositories = true
)
@ComponentScan(basePackages = {
    "vn.com.fecredit.app.repository",
    "vn.com.fecredit.app.repository.test"
})
@ActiveProfiles("test")
public class TestConfig {
}
