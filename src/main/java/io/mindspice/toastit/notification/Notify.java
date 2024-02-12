package io.mindspice.toastit.notification;

import io.mindspice.toastit.entries.event.EventEntry;

import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;
import io.mindspice.toastit.util.Util;


public class Notify {
    public static ProcessBuilder newEventNotification(Tag tag, EventEntry event) {
        return new ProcessBuilder(
                "notify-send",
                tag.notifyTitle().isEmpty() ? event.name() : tag.notifyTitle(),
                event.name() + "\t" + String.format("%s - %s",
                        DateTimeUtil.printDateTimeShort(event.startTime()),
                        DateTimeUtil.printDateTimeShort(event.endTime())),
                "-i", tag.primaryIcon(),
                "-u", event.notificationLevel().name(),
                "-t", String.valueOf(1000 * Settings.FADE_TIME_SEC)
        );
    }
}
