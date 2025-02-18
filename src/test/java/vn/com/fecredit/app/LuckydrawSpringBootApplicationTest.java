package vn.com.fecredit.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class LuckydrawSpringBootApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the application context loads correctly
    }
}