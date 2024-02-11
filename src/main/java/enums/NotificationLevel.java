package enums;

public enum NotificationLevel {
    LOW,
    NORMAL,
    CRITICAL;

    public static NotificationLevel fromString(String s) {
        for (var val : NotificationLevel.values()) {
            if (val.name().contains(s.toUpperCase())) {
                return val;
            }
        }
        return NORMAL;
    }
}
