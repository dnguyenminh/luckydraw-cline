package vn.com.fecredit.app;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.repository.test.TestDataFactory;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestDataFactory.class)
@SqlGroup({
    @Sql(
        scripts = "classpath:schema.sql",
        config = @SqlConfig(
            separator = ";",
            errorMode = SqlConfig.ErrorMode.FAIL_ON_ERROR,
            transactionMode = SqlConfig.TransactionMode.ISOLATED
        ),
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    @Sql(
        scripts = "classpath:cleanup.sql",
        config = @SqlConfig(
            separator = ";",
            errorMode = SqlConfig.ErrorMode.FAIL_ON_ERROR,
            transactionMode = SqlConfig.TransactionMode.ISOLATED
        ),
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
})
public abstract class BaseRepositoryTest {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    protected <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    protected void clear() {
        entityManager.clear();
    }

    protected void flush() {
        entityManager.flush();
    }

    protected <T> void refresh(T entity) {
        entityManager.refresh(entity);
    }
}
