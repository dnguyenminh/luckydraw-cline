package vn.com.fecredit.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.com.fecredit.app.enums.EventStatus;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class EventStatusConverter implements AttributeConverter<EventStatus, String> {

    @Override
    public String convertToDatabaseColumn(EventStatus eventStatus) {
        if (eventStatus == null) {
            return EventStatus.DRAFT.name();
        }
        return eventStatus.name();
    }

    @Override
    public EventStatus convertToEntityAttribute(String status) {
        if (status == null) {
            return EventStatus.DRAFT;
        }

        return Stream.of(EventStatus.values())
                    .filter(s -> s.name().equals(status))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown EventStatus: " + status));
    }
}
