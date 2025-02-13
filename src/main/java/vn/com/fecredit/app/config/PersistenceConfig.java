package vn.com.fecredit.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;

@Configuration
@EnableJpaRepositories(basePackages = "vn.com.fecredit.app.repository")
@EntityScan(basePackages = "vn.com.fecredit.app.model")
@EnableTransactionManagement
public class PersistenceConfig {

    // @Bean
    // public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    //     LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    //     em.setDataSource(dataSource);
    //     em.setPackagesToScan("vn.com.fecredit.app.model");

    //     HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    //     vendorAdapter.setGenerateDdl(false);
    //     em.setJpaVendorAdapter(vendorAdapter);

    //     Properties properties = new Properties();
        
    //     // Production-appropriate settings
    //     properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    //     properties.setProperty("hibernate.jdbc.batch_size", "25");
    //     properties.setProperty("hibernate.jdbc.fetch_size", "25");
        
    //     // Critical transaction settings needed in production
    //     properties.setProperty("hibernate.connection.provider_disables_autocommit", "false");
    //     properties.setProperty("hibernate.transaction.jta.platform", "org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform");
        
    //     // PostgreSQL specific settings needed for proper locking
    //     properties.setProperty("hibernate.dialect.lock.timeout", "3000"); // 3 seconds lock timeout
    //     properties.setProperty("hibernate.jdbc.time_zone", "UTC");
    //     properties.setProperty("hibernate.connection.isolation", String.valueOf(TransactionDefinition.ISOLATION_SERIALIZABLE));
        
    //     em.setJpaProperties(properties);
    //     return em;
    // }

    // @Primary
    // @Bean
    // public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    //     JpaTransactionManager transactionManager = new JpaTransactionManager();
    //     transactionManager.setEntityManagerFactory(entityManagerFactory);
    //     transactionManager.setDefaultTimeout(10); // 10 seconds for production
    //     transactionManager.setRollbackOnCommitFailure(true);
    //     return transactionManager;
    // }

    // @Bean
    // public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    //     TransactionTemplate template = new TransactionTemplate(transactionManager);
    //     template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    //     template.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    //     template.setTimeout(10); // 10 seconds for production
    //     return template;
    // }
}