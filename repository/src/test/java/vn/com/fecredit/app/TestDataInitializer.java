package vn.com.fecredit.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

@Configuration
@Profile("test")
public class TestDataInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void initialize() {
        // Test data initialization can be added here if needed
    }

    @Transactional
    public void clearDatabase() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        
        entityManager.createQuery("DELETE FROM SpinHistory").executeUpdate();
        entityManager.createQuery("DELETE FROM GoldenHour").executeUpdate();
        entityManager.createQuery("DELETE FROM Reward").executeUpdate();
        entityManager.createQuery("DELETE FROM ParticipantEvent").executeUpdate();
        entityManager.createQuery("DELETE FROM EventLocation").executeUpdate();
        entityManager.createQuery("DELETE FROM Event").executeUpdate();
        entityManager.createQuery("DELETE FROM Province").executeUpdate();
        entityManager.createQuery("DELETE FROM Region").executeUpdate();
        entityManager.createQuery("DELETE FROM Participant").executeUpdate();
        entityManager.createQuery("DELETE FROM User").executeUpdate();
        entityManager.createQuery("DELETE FROM Role").executeUpdate();
        
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
        
        entityManager.flush();
        entityManager.clear();
    }
}
