package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeMapper {
    public static LocalDateTime toLocalDateTime(LocalTime time) {
        if (time == null) return null;
        return LocalDateTime.now()
                .withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond())
                .withNano(time.getNano());
    }

    public static LocalTime toLocalTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalTime();
    }
}
