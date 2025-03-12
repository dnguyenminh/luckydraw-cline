package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventTestBuilder {
    
    public static Event createTestEvent() {
        return createTestEvent(EntityStatus.ACTIVE);
    }

    public static Event createTestEvent(EntityStatus status) {
        Event event = new Event();
        event.setName("Test Event");
        event.setCode("TEST-" + UUID.randomUUID().toString().substring(0, 8));
        event.setStartTime(LocalDateTime.now());
        event.setEndTime(LocalDateTime.now().plusDays(1));
        event.setStatus(status);
        return event;
    }

    public static Event createTestEventWithCode(String code) {
        Event event = createTestEvent();
        event.setCode(code);
        return event;
    }

    public static Event createTestEventWithName(String name) {
        Event event = createTestEvent();
        event.setName(name);
        return event;
    }

    public static Event createTestEventWithDates(LocalDateTime start, LocalDateTime end) {
        Event event = createTestEvent();
        event.setStartTime(start);
        event.setEndTime(end);
        return event;
    }
}
