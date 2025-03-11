package vn.com.fecredit.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.jdbc.SqlGroup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.com.fecredit.app.repository.test.TestDataFactory;

@DataJpaTest(includeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                "vn.com.fecredit.app.entity.*",
                "vn.com.fecredit.app.repository.*"
        })
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@EnableJpaAuditing
@ExtendWith(SpringExtension.class)
@Import(TestDataFactory.class)
@SqlGroup({
    @Sql(
        scripts = "classpath:cleanup.sql",
        config = @SqlConfig(
            separator = ";",
            errorMode = ErrorMode.FAIL_ON_ERROR,
            transactionMode = SqlConfig.TransactionMode.ISOLATED
        ),
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    @Sql(
        scripts = "classpath:schema.sql",
        config = @SqlConfig(
            separator = ";",
            errorMode = ErrorMode.FAIL_ON_ERROR,
            transactionMode = SqlConfig.TransactionMode.ISOLATED
        ),
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    // @Sql(
    //     scripts = "classpath:data.sql",
    //     config = @SqlConfig(
    //         separator = ";",
    //         errorMode = ErrorMode.FAIL_ON_ERROR,
    //         transactionMode = SqlConfig.TransactionMode.ISOLATED
    //     ),
    //     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    // )
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
