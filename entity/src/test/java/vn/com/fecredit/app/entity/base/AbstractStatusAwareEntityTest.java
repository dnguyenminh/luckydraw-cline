package vn.com.fecredit.app.entity.base;

import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.Participant;

import static org.junit.jupiter.api.Assertions.*;

class AbstractStatusAwareEntityTest extends BaseEntityTest {

    private static final Integer STATUS_ACTIVE = 1;
    private static final Integer STATUS_INACTIVE = 0;

    @Test
    void status_ShouldBeHandledByAllEntities() {
        // Test Event
        Event event = new Event();
        event.setStatus(STATUS_ACTIVE);
        assertEquals(STATUS_ACTIVE, event.getStatus());

        // Test Role
        Role role = new Role();
        role.setStatus(STATUS_ACTIVE);
        assertEquals(STATUS_ACTIVE, role.getStatus());

        // Test User
        User user = new User();
        user.setStatus(STATUS_ACTIVE);
        assertEquals(STATUS_ACTIVE, user.getStatus());

        // Test Participant
        Participant participant = new Participant();
        participant.setStatus(STATUS_ACTIVE);
        assertEquals(STATUS_ACTIVE, participant.getStatus());
    }

    @Test
    void status_ShouldHandleActiveState() {
        Event event = new Event();
        event.setStatus(STATUS_ACTIVE);
        assertEquals(STATUS_ACTIVE, event.getStatus());
    }

    @Test
    void status_ShouldHandleInactiveState() {
        Event event = new Event();
        event.setStatus(STATUS_INACTIVE);
        assertEquals(STATUS_INACTIVE, event.getStatus());
    }

//    @Test
//    void status_ShouldHandleNullValue() {
//        Event event = new Event();
//        event.setStatus(null);
//        assertNull(event.getStatus());
//    }

    @Test
    void status_ShouldBeIndependentBetweenEntities() {
        Event event1 = new Event();
        Event event2 = new Event();

        event1.setStatus(STATUS_ACTIVE);
        event2.setStatus(STATUS_INACTIVE);

        assertEquals(STATUS_ACTIVE, event1.getStatus());
        assertEquals(STATUS_INACTIVE, event2.getStatus());
    }
}
