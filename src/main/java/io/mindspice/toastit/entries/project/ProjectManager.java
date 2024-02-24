package io.mindspice.toastit.entries.project;

import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.CalendarEvents;
import io.mindspice.toastit.entries.DatedEntry;
import io.mindspice.toastit.notification.Notify;
import io.mindspice.toastit.notification.ScheduledNotification;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;


public class ProjectManager implements CalendarEvents {

    public final List<ScheduledNotification> scheduledNotifications = new CopyOnWriteArrayList<>();
    public final List<ProjectEntry> activeProjects = new CopyOnWriteArrayList<>();
    public final ScheduledExecutorService exec = App.instance().getExec();

    public void init() {
        exec.scheduleAtFixedRate(
                () -> refreshActiveProjects.accept(this),
                0,
                Settings.TASK_REFRESH_INV_MIN,
                TimeUnit.MINUTES
        );
    }

    public List<ProjectEntry> getActiveProjects() {
        return activeProjects;
    }

    public List<ProjectEntry> getAllProjects() throws IOException {
        List<ProjectEntry.Stub> stubs = App.instance().getDatabase().getAllProjects();
        List<ProjectEntry> tasks = new ArrayList<>(stubs.size());
        for (var stub : stubs) {
            try {
                tasks.add(JSON.loadObjectFromFile(stub.metaPath(), ProjectEntry.class));
            } catch (IOException e) {
                System.err.println("Failed to load: " + e.getMessage());
            }
        }
        return tasks.stream().sorted(Comparator.comparing(ProjectEntry::dueBy)).toList();
    }

    public void addProject(ProjectEntry project) throws IOException {
        App.instance().getDatabase().upsertProject(project);
        if (project.started()) {
            List<ScheduledNotification> notifications = createProjectReminders.apply(project);
            scheduledNotifications.addAll(notifications);
            activeProjects.add(project);
        }
        project.flushToDisk();
    }

    public void updateProject(ProjectEntry project) {
        try {
            removeFromScheduled(project.uuid());
            addProject(project);
            project.flushToDisk();
        } catch (IOException e) {
            System.err.println("Error deleting project: " + project.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void deleteProject(ProjectEntry project) {
        try {
            App.instance().getDatabase().deleteProjectByUUID(project.uuid());
            removeFromScheduled(project.uuid());
            Files.delete(project.getFile().toPath());
        } catch (IOException e) {
            System.err.println("Error deleting project: " + project.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void archiveProject(ProjectEntry project) {
        try {
            removeFromScheduled(project.uuid());
            App.instance().getDatabase().archiveProject(project.uuid(), true);
        } catch (IOException e) {
            System.err.println("Error archiving project: " + project.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void removeFromScheduled(UUID uuid) {
        activeProjects.removeIf(t -> t.uuid().equals(uuid));
        List<ScheduledNotification> notifications = scheduledNotifications.stream()
                .filter(sn -> sn.uuid().equals(uuid)).toList();
        scheduledNotifications.removeAll(notifications);
    }

    public Function<ProjectEntry, List<ScheduledNotification>> createProjectReminders = (project) -> {

        List<ScheduledNotification> newNotifications = new ArrayList<>();
        project.reminders().forEach(reminder -> {
            ProcessBuilder notification = Notify.newDueByNotify(
                    project.tags().isEmpty() ? Tag.Default() : Settings.getTag(project.tags().getFirst()),
                    project,
                    reminder.level()
            );

            Runnable notifyProject = () -> {
                try {
                    notification.start();
                    scheduledNotifications.removeIf(sn -> sn.uuid().equals(project.uuid()) && sn.time().equals(reminder.time()));
                } catch (IOException e) {
                    System.err.printf("Error emitting notification for: %s, Error: %s%n", project, e);
                }
            };
            var sf = exec.schedule(notifyProject, DateTimeUtil.delayToDateTime(reminder.time()), TimeUnit.SECONDS);
            newNotifications.add(new ScheduledNotification(project.uuid(), reminder.time(), sf));
        });
        return newNotifications;

    };

    public Consumer<ProjectManager> refreshActiveProjects = (self) -> {
        try {
            activeProjects.clear();
            List<ProjectEntry.Stub> projectStubs = App.instance().getDatabase().getActiveProjects();
            List<ProjectEntry> fullProjects = new ArrayList<>(projectStubs.size());
            for (var stub : projectStubs) {
                try {
                    fullProjects.add(JSON.loadObjectFromFile(stub.metaPath(), ProjectEntry.class));
                } catch (IOException e) {
                    System.err.println("Failed to load: " + e.getMessage());
                }
            }

            activeProjects.addAll(fullProjects.stream().sorted(Comparator.comparing(ProjectEntry::dueBy)).toList());

            List<ScheduledNotification> notifications = activeProjects.stream()
                    .flatMap(project -> createProjectReminders.apply(project).stream())
                    .toList();

            scheduledNotifications.addAll(notifications);
        } catch (IOException e) {
            System.err.printf("Failed to refresh active projects: %s%n%s%n",
                    e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    };

    @Override
    public List<String> getCalendarEvents(LocalDate date, Function<DatedEntry, String> dataMapper) {
        return activeProjects.stream()
                .filter(e -> e.dueBy().isEqual(date.atStartOfDay()))
                .map(dataMapper)
                .toList();
    }
}
