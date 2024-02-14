package io.mindspice.toastit.entries.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;


public record EventEntry(
        UUID uuid,
        String name,
        List<String> tags,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<Reminder> reminders,
        UUID linkedUUID,
        boolean completed
) implements Entry {
    public EventEntry asCompleted() {
        return new EventEntry(uuid, name, tags, startTime, endTime, reminders, linkedUUID, true);
    }

    public EventEntry {
        startTime = startTime.truncatedTo(ChronoUnit.MINUTES);
        endTime = endTime.truncatedTo(ChronoUnit.MINUTES);
        linkedUUID = linkedUUID == null ? UUID.fromString("00000000-0000-0000-0000-000000000000") : linkedUUID;
    }

    public Stub getStub() throws JsonProcessingException {
        return new Stub(
                uuid.toString(),
                name,
                JSON.writeString(tags),
                startTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                endTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                JSON.writeString(reminders.stream().map(Reminder::getStub).toList()),
                linkedUUID.toString(),
                completed
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    @Override
    public EntryType type() {
        return EntryType.EVENT;
    }

    @Override
    public String shortText() {
        return name + "|" + DateTimeUtil.printDateTimeFull(startTime);
    }

    public static class Builder {
        public UUID uuid = UUID.randomUUID();
        public String name = "";
        public List<String> tags = new ArrayList<>();
        public LocalDateTime startTime = LocalDateTime.MAX;
        public LocalDateTime endTime = LocalDateTime.MAX;
        public List<Reminder> reminders = new ArrayList<>();
        public UUID linkedUUID = Util.NULL_UUID;
        public boolean completed = false;

        public Builder() { }

        public Builder(EventEntry e) {
            this.uuid = e.uuid;
            this.name = e.name;
            this.tags = new ArrayList<>(e.tags);
            this.startTime = e.startTime;
            this.endTime = e.endTime;
            this.reminders = e.reminders;
            this.linkedUUID = e.linkedUUID;
            this.completed = e.completed;
        }

        public EventEntry build() {
            return new EventEntry(
                    uuid,
                    name,
                    Collections.unmodifiableList(tags),
                    startTime,
                    endTime,
                    reminders,
                    linkedUUID,
                    completed
            );
        }

        public List<Pair<String, String>> toTableState() {
            List<Pair<String, String>> rntList = new ArrayList<>(5);
            if (!name.isEmpty()) {
                rntList.add(Pair.of("Name", name));
            }

            if (!startTime.equals(LocalDateTime.MAX)) {
                rntList.add(Pair.of("Start Time", DateTimeUtil.printDateTimeFull(startTime)));
            }
            if (!endTime.equals(LocalDateTime.MAX)) {
                rntList.add(Pair.of("End Time", DateTimeUtil.printDateTimeFull(endTime)));
            }
            if (!tags.isEmpty()) {
                rntList.add(Pair.of("Tags", tags.toString()));
            }
            if (!reminders.isEmpty()) {
                IntStream.range(0, reminders.size()).forEach(i -> rntList.add(
                        Pair.of(String.format("Reminder %d", i + 1), reminders.get(i).toString())
                ));
            }
            return rntList;
        }
    }


    public record Stub(
            String uuid,
            String name,
            String tags,
            long startTime,
            long endTime,
            String reminders,
            String linkedUUID,
            boolean completed
    ) { }
}
