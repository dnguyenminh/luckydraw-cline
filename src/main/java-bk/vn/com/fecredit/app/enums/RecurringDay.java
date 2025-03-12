package vn.com.fecredit.app.enums;

public enum RecurringDay {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    private final int value;

    RecurringDay(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RecurringDay fromValue(int value) {
        for (RecurringDay day : RecurringDay.values()) {
            if (day.getValue() == value) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid RecurringDay value: " + value);
    }
}
