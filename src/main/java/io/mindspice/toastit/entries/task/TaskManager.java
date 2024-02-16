package io.mindspice.toastit.entries.task;

import io.mindspice.toastit.App;
import io.mindspice.toastit.notification.Notify;
import io.mindspice.toastit.notification.ScheduledNotification;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;


public class TaskManager {
    public final List<ScheduledNotification> scheduledNotifications = new CopyOnWriteArrayList<>();
    public final List<TaskEntry> activeTasks = new CopyOnWriteArrayList<>();
    public final ScheduledExecutorService exec = App.instance().getExec();

    public void init() {
        exec.scheduleAtFixedRate(
                () -> refreshActiveTasks.accept(this),
                0,
                Settings.TASK_REFRESH_INV_MIN,
                TimeUnit.MINUTES
        );
    }

    public List<TaskEntry> getActiveTasks() {
        return activeTasks;
    }

    public List<TaskEntry> getAllUncompleted() throws IOException {
        List<TaskEntry.Stub> stubs = App.instance().getDatabase().getAllTasks();
        List<TaskEntry> tasks = new ArrayList<>(stubs.size());
        for (var stub : stubs) {
            try {
                tasks.add(JSON.loadObjectFromFile(stub.metaPath(), TaskEntry.class));
            } catch (IOException e) {
                System.err.println("Failed to load: " + e.getMessage());
            }
        }
        return tasks.stream().sorted(Comparator.comparing(TaskEntry::dueBy)).toList();
    }

    public void addTask(TaskEntry task) throws IOException {
        App.instance().getDatabase().upsertTask(task, false);
        if (task.started()) {
            List<ScheduledNotification> notifications = createTaskReminders.apply(task);
            scheduledNotifications.addAll(notifications);
            activeTasks.add(task);
        }
        task.flushToDisk();
    }

    public void updateTask(TaskEntry task) {
        try {
            removeFromScheduled(task.uuid());
            addTask(task);
            task.flushToDisk();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error deleting task: " + task.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void deleteTask(TaskEntry task) {
        try {
            App.instance().getDatabase().deleteTaskByUUID(task.uuid());
            removeFromScheduled(task.uuid());
            Files.delete(task.getFile().toPath());
        } catch (IOException e) {
            System.err.println("Error deleting task: " + task.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void archiveTask(TaskEntry task) throws IOException {
        App.instance().getDatabase().deleteTaskByUUID(task.uuid());
        removeFromScheduled(task.uuid());
        App.instance().getDatabase().upsertTask(task, true);
    }

    public void removeFromScheduled(UUID uuid) {
        activeTasks.removeIf(t -> t.uuid().equals(uuid));
        List<ScheduledNotification> notifications = scheduledNotifications.stream()
                .filter(sn -> sn.uuid().equals(uuid)).toList();
        scheduledNotifications.removeAll(notifications);
    }

    public Function<TaskEntry, List<ScheduledNotification>> createTaskReminders = (task) -> {

        List<ScheduledNotification> newNotifications = new ArrayList<>();
        task.reminders().forEach(reminder -> {
            ProcessBuilder notification = Notify.newTaskNotify(
                    task.tags().isEmpty() ? Tag.Default() : Settings.getTag(task.tags().getFirst()),
                    task,
                    reminder.level()
            );

            Runnable notifyTask = () -> {
                //FIXME
//                try {
//                    notification.start();
//                    scheduledNotifications.removeIf(sn -> sn.uuid().equals(task.uuid()) && sn.time().equals(reminder.time()));
//                } catch (IOException e) {
//                    System.err.printf("Error emitting notification for: %s, Error: %s%n", task, e);
//                }
            };
            var sf = exec.schedule(notifyTask, DateTimeUtil.delayToDateTime(reminder.time()), TimeUnit.SECONDS);
            newNotifications.add(new ScheduledNotification(task.uuid(), reminder.time(), sf));
        });
        return newNotifications;

    };

    public Consumer<TaskManager> refreshActiveTasks = (self) -> {
        try {
            activeTasks.clear();
            List<TaskEntry.Stub> taskStubs = App.instance().getDatabase().getActiveTasks();
            List<TaskEntry> fullTasks = new ArrayList<>(taskStubs.size());
            for (var stub : taskStubs) {
                try {
                    fullTasks.add(JSON.loadObjectFromFile(stub.metaPath(), TaskEntry.class));
                } catch (IOException e) {
                    System.err.println("Failed to load: " + e.getMessage());
                }
            }

            activeTasks.addAll(fullTasks.stream().sorted(Comparator.comparing(TaskEntry::dueBy)).toList());

            List<ScheduledNotification> notifications = activeTasks.stream()
                    .flatMap(task -> createTaskReminders.apply(task).stream())
                    .toList();

            scheduledNotifications.addAll(notifications);
        } catch (IOException e) {
            System.err.printf("Failed to refresh active tasks: %s%n%s%n",
                    e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    };
}
