package entries.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import enums.NotificationLevel;
import util.JSON;
import util.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;


public record EventEntry(
        UUID uuid,
        String name,
        List<String> tags,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<LocalDateTime> reminders,
        NotificationLevel notificationLevel,
        boolean completed
) {
    public EventEntry asCompleted() {
        return new EventEntry(uuid, name, tags, startTime, endTime, reminders, notificationLevel, true);
    }

    public EventEntry {
        startTime = startTime.truncatedTo(ChronoUnit.MINUTES);
        endTime = endTime.truncatedTo(ChronoUnit.MINUTES);
        reminders = reminders == null ? List.of() : reminders.stream().map(r -> r.truncatedTo(ChronoUnit.MINUTES)).toList();
    }

    public Stub getStub() throws JsonProcessingException {
        return new Stub(
                uuid.toString(),
                name,
                JSON.writeString(tags),
                startTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                endTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                JSON.writeString(reminders.stream().map(Util::localToUnix).toList()),
                notificationLevel,
                completed
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private UUID uuid = UUID.randomUUID();
        private String name = "Unnamed";
        private List<String> tags = new ArrayList<>();
        private LocalDateTime startTime = LocalDateTime.MAX;
        private LocalDateTime endTime = LocalDateTime.MAX;
        private List<LocalDateTime> reminders;
        private NotificationLevel notificationLevel;
        private boolean completed = false;

        public Builder() { }

        public Builder(EventEntry e) {
            this.name = e.name;
            this.tags = e.tags;
            this.startTime = e.startTime;
            this.endTime = e.endTime;
            this.completed = e.completed;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder addTag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder removeTag(String tag) {
            this.tags.remove(tag);
            return this;
        }

        public Builder addReminder(LocalDateTime reminderTIme) {
            this.reminders.add(reminderTIme);
            return this;
        }

        public Builder removeReminder(LocalDateTime reminderTime) {
            this.reminders.remove(reminderTime);
            return this;
        }

        public Builder setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder setCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder setNotificationLevel(NotificationLevel notificationLevel) {
            this.notificationLevel = notificationLevel;
            return this;
        }

        public EventEntry build() {
            return new EventEntry(
                    uuid,
                    name,
                    Collections.unmodifiableList(tags),
                    startTime,
                    endTime,
                    reminders,
                    notificationLevel,
                    completed
            );
        }
    }


    public record Stub(
            String uuid,
            String name,
            String tags,
            long startTime,
            long endTime,
            String reminderTimes,
            NotificationLevel notificationLevel,
            boolean completed
    ) { }
}
