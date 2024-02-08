package notification;

import enums.NotificationLevel;
import enums.NotificationType;


public class Notify {
    public static ProcessBuilder newNotification(String title, NotificationType notifyType,
            NotificationLevel notifyLevel, String text, int fadeTime) {
        return new ProcessBuilder("notify-send", title, text, "-i", notifyType.value, "-u",
                notifyLevel.name(), "-t", String.valueOf(fadeTime));
    }
}
