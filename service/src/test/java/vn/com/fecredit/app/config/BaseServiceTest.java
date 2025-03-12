package vn.com.fecredit.app.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Transactional
public abstract class BaseServiceTest {

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanAndPopulateData() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setScripts(
            new ClassPathResource("schema-test.sql"),
            new ClassPathResource("cleanup.sql"),
            new ClassPathResource("data-test.sql")
        );
        populator.setSeparator(";");
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.execute(dataSource);
    }
}
