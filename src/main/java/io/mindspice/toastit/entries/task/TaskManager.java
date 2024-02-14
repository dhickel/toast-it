package io.mindspice.toastit.entries.task;

import io.mindspice.toastit.App;
import io.mindspice.toastit.notification.Notify;
import io.mindspice.toastit.notification.ScheduledNotification;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;


public class TaskManager {
    public final List<ScheduledNotification> scheduledNotifications = new CopyOnWriteArrayList<>();
    public final List<TaskEntry.Stub> activeTasks = new CopyOnWriteArrayList<>();
    public final ScheduledExecutorService exec = App.instance().getExec();

    public void init() {
        exec.scheduleAtFixedRate(
                () -> refreshActiveTasks.accept(this),
                0,
                Settings.TASK_REFRESH_INV_MIN,
                TimeUnit.MINUTES
        );
    }

    public List<TaskEntry.Stub> getActiveTasks() {
        return activeTasks;
    }

    public void addTask(TaskEntry task) throws IOException {
        App.instance().getDatabase().upsertTask(task);
        if (task.started()) {
            TaskEntry.Stub taskStub = task.getStub();
            List<ScheduledNotification> notifications = createTaskReminders.apply(taskStub);
            scheduledNotifications.addAll(notifications);
            activeTasks.add(taskStub);
        }
    }

    public void updateTask(TaskEntry task) throws IOException {
        removeFromScheduled(task.uuid());
        addTask(task);
    }

    public void deleteTask(UUID uuid) throws IOException {
        App.instance().getDatabase().deleteTaskByUUID(uuid);
        removeFromScheduled(uuid);
    }

    public void removeFromScheduled(UUID uuid) {
        activeTasks.removeIf(t -> t.uuid().equals(uuid.toString()));
        List<ScheduledNotification> notifications = scheduledNotifications.stream()
                .filter(sn -> sn.uuid().equals(uuid)).toList();
        scheduledNotifications.removeAll(notifications);
    }

    public Function<TaskEntry.Stub, List<ScheduledNotification>> createTaskReminders = (task) -> {
        var reminders = JSON.jsonArrayToReminderList(task.reminders());
        var tags = JSON.jsonArrayToStringList(task.tags());
        var uuid = UUID.fromString(task.uuid());

        List<ScheduledNotification> newNotifications = new ArrayList<>();
        reminders.forEach(reminder -> {
            ProcessBuilder notification = Notify.newTaskNotify(
                    tags.isEmpty() ? Tag.Default() : Settings.getTag(tags.getFirst()),
                    task,
                    reminder.level()
            );

            Runnable notifyTask = () -> {
                try {
                    notification.start();
                    scheduledNotifications.removeIf(sn -> sn.uuid().equals(uuid) && sn.time().equals(reminder.time()));
                } catch (IOException e) {
                    System.err.printf("Error emitting notification for: %s, Error: %s%n", task, e);
                }
            };
            var sf = exec.schedule(notifyTask, DateTimeUtil.delayToDateTime(reminder.time()), TimeUnit.SECONDS);
            newNotifications.add(new ScheduledNotification(uuid, reminder.time(), sf));
        });
        return newNotifications;

    };

    public Consumer<TaskManager> refreshActiveTasks = (self) -> {
        try {
            activeTasks.clear();
            activeTasks.addAll(App.instance().getDatabase().getActiveTasks());

            List<ScheduledNotification> notifications = activeTasks.stream()
                    .flatMap(task -> createTaskReminders.apply(task).stream())
                    .toList();

            scheduledNotifications.addAll(notifications);
        } catch (IOException e) {
            System.err.println("Failed to refresh active tasks");
        }
    };
}
