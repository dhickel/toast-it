package io.mindspice.toastit.entries.event;

import io.mindspice.mindlib.data.tuples.Triple;
import io.mindspice.toastit.App;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.notification.Notify;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.notification.ScheduledNotification;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.Tag;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class EventManager {
    public List<ScheduledNotification> scheduledNotifications = new CopyOnWriteArrayList<>();
    public final List<EventEntry> pastEvents = new CopyOnWriteArrayList<>();
    public final List<EventEntry> futureEvents = new CopyOnWriteArrayList<>();
    public final ScheduledExecutorService exec = App.instance().getExec();
    public volatile long lastEventReCalc = Instant.now().getEpochSecond();

    public void init() {
        exec.scheduleAtFixedRate(
                () -> refreshEventNotifications.accept(this),
                0,
                Settings.EVENT_REFRESH_INV_MIN,
                TimeUnit.MINUTES
        );
    }

    public List<EventEntry> getPastEvents() {
        if (Instant.now().getEpochSecond() > lastEventReCalc + 60) {
            reCalcEventsLists();
        }
        return pastEvents;
    }

    public List<EventEntry> getFutureEvents() {
        if (Instant.now().getEpochSecond() > lastEventReCalc + 60) {
            reCalcEventsLists();
        }
        return futureEvents;
    }

    public void reCalcEventsLists() {
        List<EventEntry> newPastEvents = futureEvents.stream().filter(e -> e.endTime().isBefore(LocalDateTime.now())).toList();
        pastEvents.addAll(newPastEvents);
        futureEvents.removeAll(newPastEvents);
    }

    public void clearPastEvents() throws IOException {
        App.instance().getDatabase().deletePastEventEntries(Instant.now().getEpochSecond());
        pastEvents.clear();
    }

    public void addEvent(EventEntry event) throws IOException {
        App.instance().getDatabase().upsertEvent(event, false);
        int lookForwardDays = Settings.EVENT_LOOK_FORWARD_DAYS;
        if (lookForwardDays == -1 || event.startTime().isAfter(LocalDateTime.now().minusDays(lookForwardDays))) {
            List<ScheduledNotification> notifications = createEventReminders.apply(event);
            scheduledNotifications.addAll(notifications);
            futureEvents.add(event);
        }
    }

    public void updateEvent(EventEntry event) {
        try {
            removeFromScheduled(event.uuid());
            addEvent(event);
        } catch (IOException e) {
            System.err.println("Error deleting task: " + event.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void deleteEvent(EventEntry event) {
        try {
            App.instance().getDatabase().deleteEventByUUID(event.uuid());
            removeFromScheduled(event.uuid());
        } catch (IOException e) {
            System.err.println("Error deleting Event: " + event.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void archiveEvent(EventEntry task) {
        try {
            App.instance().getDatabase().deleteEventByUUID(task.uuid());
            removeFromScheduled(task.uuid());
            App.instance().getDatabase().upsertEvent(task, true);
        } catch (IOException e) {
            System.err.println("Error archiving task: " + task.uuid() + "| " + Arrays.toString(e.getStackTrace()));
        }
    }

    public List<EventEntry> getAllEvents() {
        try {
            return App.instance().getDatabase().getEvents(-1);
        } catch (IOException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            return List.of();
        }
    }

    public void removeFromScheduled(UUID uuid) {
        futureEvents.removeIf(e -> e.uuid().equals(uuid));
        pastEvents.removeIf(e -> e.uuid().equals(uuid));
        List<ScheduledNotification> notifications = scheduledNotifications.stream()
                .filter(sn -> sn.uuid().equals(uuid)).toList();
        scheduledNotifications.removeAll(notifications);
    }

    public Function<EventEntry, List<ScheduledNotification>> createEventReminders = (EventEntry event) -> {
        Tag tag = event.tags().isEmpty()
                ? Tag.Default()
                : Settings.TAG_MAP.getOrDefault(event.tags().getFirst(), Tag.Default());

        List<ScheduledNotification> newNotifications = new ArrayList<>(4);
        event.reminders().forEach(reminder -> {
            if (scheduledNotifications.stream().noneMatch(
                    sn -> sn.uuid().equals(event.uuid()) && sn.time().equals(reminder.time()))) {

                ProcessBuilder notification = Notify.newEventNotify(tag, event, reminder.level());
                Runnable notifyTask = () -> {
                    //FIXME
//                    try {
//                        notification.start();
//                        scheduledNotifications.removeIf(sn -> sn.uuid().equals(event.uuid()) && sn.time().equals(reminder.time()));
//                    } catch (IOException e) {
//                        System.err.printf("Error emitting notification for: %s, Error: %s%n", event, e);
//                    }
                };

                var sf = exec.schedule(notifyTask, DateTimeUtil.delayToDateTime(reminder.time()), TimeUnit.SECONDS);
                newNotifications.add(new ScheduledNotification(event.uuid(), reminder.time(), sf));
            }
        });
        return newNotifications;
    };

    public Consumer<EventManager> refreshEventNotifications = (self) -> {

        try {

            long lookFoward = Settings.EVENT_LOOK_FORWARD_DAYS == -1
                    ? -1
                    : DateTimeUtil.localToUnix(LocalDateTime.now().plusDays(Settings.EVENT_LOOK_FORWARD_DAYS));

            List<EventEntry> events = App.instance().getDatabase().getEvents(lookFoward);

            Map<Boolean, List<EventEntry>> mappedEvents = events.stream()
                    .collect(Collectors.groupingBy(c -> c.endTime().isAfter(LocalDateTime.now())));

            pastEvents.clear();
            pastEvents.addAll(mappedEvents.getOrDefault(Boolean.FALSE, List.of()).stream()
                    .sorted(Comparator.comparing(EventEntry::startTime)).toList());

            futureEvents.clear();
            futureEvents.addAll(mappedEvents.getOrDefault(Boolean.TRUE, List.of()).stream()
                    .sorted(Comparator.comparing(EventEntry::startTime)).toList());

            List<ScheduledNotification> notifications = futureEvents.stream()
                    .flatMap(event -> createEventReminders.apply(event).stream())
                    .toList();

            scheduledNotifications.addAll(notifications);


        } catch (IOException e) {
            System.err.println("Failed to refresh events: " + e.getMessage());
        }
    };
}



