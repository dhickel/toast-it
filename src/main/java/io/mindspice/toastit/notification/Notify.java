package io.mindspice.toastit.notification;

import io.mindspice.toastit.entries.event.EventEntry;

import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;


public class Notify {
    public static ProcessBuilder newEventNotification(Tag tag, EventEntry event, NotificationLevel level) {
        return new ProcessBuilder(
                "notify-send",
                tag.notifyTitle().isEmpty() ? event.name() : tag.notifyTitle(),
                event.name() + "\t" + String.format("%s - %s",
                        DateTimeUtil.printDateTimeShort(event.startTime()),
                        DateTimeUtil.printDateTimeShort(event.endTime())),
                "-i", tag.icon(),
                "-u", level.name(),
                "-t", String.valueOf(1000 * Settings.EVENT_NOTIFY_FADE_TIME_SEC)
        );
    }


}
