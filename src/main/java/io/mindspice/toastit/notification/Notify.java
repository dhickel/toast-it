package io.mindspice.toastit.notification;

import io.mindspice.toastit.entries.event.EventEntry;

import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;


public class Notify {
    public static ProcessBuilder newEventNotify(Tag tag, EventEntry event, NotificationLevel level) {
        return new ProcessBuilder(
                "notify-send",
                tag.notifyTitle().isEmpty() ? event.name() : tag.notifyTitle(),
                event.name() + " | " + String.format("%s - %s",
                        DateTimeUtil.printDateTimeShort(event.startTime()),
                        DateTimeUtil.printDateTimeShort(event.endTime())),
                "-i", tag.icon(),
                "-u", level.name(),
                "-t", String.valueOf(1000 * Settings.EVENT_NOTIFY_FADE_TIME_SEC)
        );
    }

    public static ProcessBuilder newTaskNotify(Tag tag, TaskEntry.Stub task, NotificationLevel level) {
        return new ProcessBuilder(
                "notify-send",
                tag.notifyTitle().isEmpty() ? task.name() : tag.notifyTitle(),
                task.name() + " | " + "Due By: " + DateTimeUtil.printDateTimeShort(DateTimeUtil.unixToLocal(task.dueBy())),
                "-i", tag.icon(),
                "-u", level.name(),
                "-t", String.valueOf(1000 * Settings.TASK_NOTIFY_FADE_TIME_SEC)
        );
    }


}
