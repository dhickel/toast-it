package entries.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import enums.EntryType;
import util.JSON;
import util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;


public record TaskEntry(
        String name,
        boolean started,
        boolean completed,
        List<SubTaskEntry> subtasks,
        String description,
        List<String> notes,
        Set<String> tags,
        LocalDateTime dueBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        UUID uuid,
        Path basePath
) implements Entry<TaskEntry> {

    public TaskEntry {
        dueBy = dueBy.truncatedTo(ChronoUnit.MINUTES);
        startedAt = startedAt.truncatedTo(ChronoUnit.MINUTES);
        completedAt = completedAt.truncatedTo(ChronoUnit.MINUTES);
        try {
            basePath = basePath == null ? Util.getEntriesPath(EntryType.TASK) : basePath;
        } catch (IOException e) {
            System.out.println("Error creating path for: " + this);
        }

    }

    public void flushToDisk() {
        if (!Files.exists(basePath)) {
            throw new IllegalStateException("Failed to update on disk, base path does not exists: " + basePath);
        }
        // Always write meta file on change
        Path metaFilePath = basePath.resolve(uuid + ".task");
        try {
            String metaJson = JSON.writePretty(this);
            Files.writeString(metaFilePath, metaJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write task file: " + metaFilePath, e);
        }
    }

    public Stub getStub() throws JsonProcessingException {
        return new Stub(
                uuid.toString(),
                name,
                started,
                completed,
                JSON.writeString(tags),
                dueBy.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                startedAt.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                completedAt.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                getFile().toString()
        );
    }

    public File getFile() {
        return basePath.resolve(uuid + ".task").toFile();
    }

    public TaskEntry asCompleted(LocalDateTime completedTime) {
        return new TaskEntry(
                name, started, true, subtasks, description, notes,
                tags, dueBy, startedAt, completedTime, uuid, basePath
        );
    }

    public TaskEntry asStarted(LocalDateTime startTime) {
        return new TaskEntry(
                name, true, completed, subtasks, description, notes,
                tags, dueBy, startTime, completedAt, uuid, basePath
        );
    }

    @Override
    public boolean completed() {
        return subtasks.isEmpty()
                ? completed
                : subtasks.stream().allMatch(Entry::completed);
    }

    @Override
    public double completionDbl() {
        return subtasks.isEmpty()
                ? (completed ? 1 : 0)
                : subtasks.stream().mapToDouble(Entry::completionDbl).average().orElse(1);
    }

    @Override
    public String completionPct() {
        return util.Util.toPercentage(completionDbl());
    }

    @Override
    public String terminalText() {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (object == null || getClass() != object.getClass()) { return false; }

        TaskEntry fullTaskEntry = (TaskEntry) object;

        return Objects.equals(uuid, fullTaskEntry.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String name = "Unnamed";
        private boolean started = false;
        private boolean completed = false;
        private List<SubTaskEntry> subtasks = new ArrayList<>();
        private String description = "";
        private List<String> notes = new ArrayList<>();
        private Set<String> tags = new HashSet<>();
        private LocalDateTime dueBy = LocalDateTime.MAX;
        private LocalDateTime startedAt = LocalDateTime.MAX;
        private LocalDateTime completedAt = LocalDateTime.MAX;
        private UUID uuid = UUID.randomUUID();
        private Path basePath;

        public Builder() { }

        public Builder(TaskEntry t) {
            this.name = t.name;
            this.started = t.started;
            this.completed = t.completed;
            this.subtasks = t.subtasks;
            this.description = t.description;
            this.notes = t.notes;
            this.tags = t.tags;
            this.dueBy = t.dueBy;
            this.startedAt = t.startedAt;
            this.completedAt = t.completedAt;
            this.uuid = t.uuid;
            this.basePath = t.basePath;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setStarted(boolean started) {
            this.started = started;
            return this;
        }

        public Builder setCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder setSubtasks(List<SubTaskEntry> subtasks) {
            this.subtasks = subtasks;
            return this;
        }

        public Builder addSubTask(SubTaskEntry subTaskEntry) {
            this.subtasks.add(subTaskEntry);
            return this;
        }

        public Builder removeSubTask(SubTaskEntry subTaskEntry) {
            this.subtasks.remove(subTaskEntry);
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setNotes(List<String> notes) {
            this.notes = notes;
            return this;
        }

        public Builder addNote(String note) {
            this.notes.add(note);
            return this;
        }

        public Builder removeNote(String note) {
            this.notes.remove(note);
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

        public Builder removeTag(String tag) {
            this.tags.remove(tag);
            return this;
        }

        public Builder setDueBy(LocalDateTime dueBy) {
            this.dueBy = dueBy;
            return this;
        }

        public Builder setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public TaskEntry build() throws IOException {
            if (basePath == null) {
                basePath = Util.getEntriesPath(EntryType.TASK);
            }
            return new TaskEntry(
                    name,
                    started,
                    completed,
                    Collections.unmodifiableList(subtasks),
                    description,
                    Collections.unmodifiableList(notes),
                    Collections.unmodifiableSet(tags),
                    dueBy,
                    startedAt,
                    completedAt,
                    uuid,
                    basePath
            );
        }
    }


    public record Stub(
            String uuid,
            String name,
            boolean started,
            boolean completed,
            String tags,
            long dueBy,
            long startedAt,
            long completedAt,
            String metaPath
    ) { }
}
