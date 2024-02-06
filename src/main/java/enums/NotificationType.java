package enums;

public enum NotificationType {
    APPT_SOON("appointment-soon", "Upcoming Appointment"),
    APPT_MISSED("appointment-missed", "Missed Appointment"),
    TASK_DUE("task-due", "Task Due"),
    TASK_MISSED("task-past-due", "Task Past Due"),
    INFO("dialog-info", "Notification" );

    public final String value;
    private final String title;

    NotificationType(String value, String title) {
        this.value = value;
        this.title = title;
    }
}
