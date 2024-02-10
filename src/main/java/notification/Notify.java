package notification;

import entries.event.EventEntry;
import enums.NotificationLevel;
import enums.NotificationType;

import util.Settings;
import util.Tag;
import util.Util;

import java.io.IOException;


public class Notify {
    public static ProcessBuilder newEventNotification(Tag tag, EventEntry event) {
        return new ProcessBuilder(
                "notify-send",
                tag.notifyTitle().isEmpty() ? event.name() : tag.notifyTitle(),
                event.name() + "\t" + String.format("%s - %s",
                        Util.dateTimeFormat().format(event.startTime()),
                        Util.dateTimeFormat().format(event.endTime())),
                "-i", tag.primaryIcon(),
                "-u", event.notificationLevel().name(),
                "-t", String.valueOf(1000 * Settings.FADE_TIME_SEC)
        );
    }
}
