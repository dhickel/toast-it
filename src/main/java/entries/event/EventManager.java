package entries.event;

import application.App;
import io.mindspice.mindlib.data.tuples.Pair;
import notification.Notify;
import util.Settings;
import util.Tag;
import util.Util;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class EventManager {
    public Map<Pair<UUID, LocalDateTime>, ScheduledFuture<?>> scheduledEvents = new ConcurrentHashMap<>();
    public final List<EventEntry> pastEvents = new CopyOnWriteArrayList<>();
    public final List<EventEntry> futureEvents = new CopyOnWriteArrayList<>();
    public final ScheduledExecutorService exec = App.instance().getExec();
    public volatile long lastEventReCalc = Instant.now().getEpochSecond();

    public void init() {
        exec.scheduleAtFixedRate(() -> refreshEventNotifications.accept(this), 0, Settings.EVENT_REFRESH_INV_MIN, TimeUnit.MINUTES);
    }

    public List<EventEntry> getPastEvents() {
        if (Instant.now().getEpochSecond() > lastEventReCalc + 60) {
            reCalcEventsLists();
        }
        return Collections.unmodifiableList(pastEvents);
    }

    public List<EventEntry> getFutureEvents() {
        if (Instant.now().getEpochSecond() > lastEventReCalc + 60) {
            reCalcEventsLists();
        }
        return Collections.unmodifiableList(futureEvents);
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
        App.instance().getDatabase().upsertEvent(event);
        if (event.startTime().isAfter(LocalDateTime.now().minusDays(Settings.EVENT_LOOK_FORWARD_DAYS))) {
            createEventReminders(event);
            futureEvents.add(event);
        }
    }

    public Consumer<EventManager> refreshEventNotifications = (self) -> {
        try {
            long lookFoward = Settings.EVENT_LOOK_FORWARD_DAYS == -1
                    ? -1
                    : Util.localToUnix(LocalDateTime.now().plusDays(Settings.EVENT_LOOK_FORWARD_DAYS));

            List<EventEntry> events = App.instance().getDatabase().getEvents(lookFoward);

            Map<Boolean, List<EventEntry>> mappedEvents = events.stream()
                    .collect(Collectors.groupingBy(c -> c.endTime().isAfter(LocalDateTime.now())));

            pastEvents.clear();
            pastEvents.addAll(mappedEvents.get(Boolean.FALSE).stream()
                    .sorted(Comparator.comparing(EventEntry::startTime)).toList());

            futureEvents.clear();
            futureEvents.addAll(mappedEvents.get(Boolean.TRUE).stream()
                    .sorted(Comparator.comparing(EventEntry::startTime)).toList());

            futureEvents.forEach(this::createEventReminders);

        } catch (IOException e) {
            System.err.println("Failed to refresh events: " + e.getMessage());
        }
    };

    public void createEventReminders(EventEntry event) {
        Tag tag = event.tags().isEmpty()
                ? Tag.Default()
                : Settings.TAG_MAP.getOrDefault(event.tags().getFirst(), Tag.Default());

        ProcessBuilder notification = Notify.newEventNotification(tag, event);

        event.reminders().forEach(time -> {
            Pair<UUID, LocalDateTime> reminderKey = Pair.of(event.uuid(), time);
            if (scheduledEvents.containsKey(reminderKey) || time.isAfter(LocalDateTime.now())) {
                return;
            }

            Runnable notifyTask = () -> {
                try {
                    notification.start();
                    scheduledEvents.remove(reminderKey);
                } catch (IOException e) {
                    System.err.printf("Error emitting notification for: %s, Error: %s%n", event, e);
                }
            };

            var sf = exec.schedule(notifyTask, Util.delayToDateTime(time), TimeUnit.SECONDS);
            scheduledEvents.put(reminderKey, sf);
        });
    }
}



