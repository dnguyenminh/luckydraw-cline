package vn.com.fecredit.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import vn.com.fecredit.app.config.TestConfig;

@SpringBootTest
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
class LuckydrawSpringBootApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the application context loads correctly
    }
}