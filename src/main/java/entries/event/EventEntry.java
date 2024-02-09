package entries.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import util.JSON;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;


public record EventEntry(
        UUID uuid,
        String name,
        Set<String> tags,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean completed
) {
    public EventEntry asCompleted() {
        return new EventEntry(uuid, name, tags, startTime, endTime, true);
    }

    public EventEntry{
        startTime = startTime.truncatedTo(ChronoUnit.MINUTES);
        endTime = endTime.truncatedTo(ChronoUnit.MINUTES);
    }

    public Stub getStub() throws JsonProcessingException {
        return new Stub(
                uuid.toString(),
                name,
                JSON.writeString(tags),
                startTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                endTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
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
        private Set<String> tags = new HashSet<>();
        private LocalDateTime startTime = LocalDateTime.MAX;
        private LocalDateTime endTime = LocalDateTime.MAX;
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

        public Builder setTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder addTag(String tag) {
            this.tags.add(tag);
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

        public EventEntry build() {
            return new EventEntry(
                    uuid,
                    name,
                    Collections.unmodifiableSet(tags),
                    startTime,
                    endTime,
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
            boolean completed
    ) { }
}
