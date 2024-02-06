package todo;

import enums.EntryType;
import enums.Month;
import enums.Year;
import io.mindspice.mindlib.util.JsonUtils;
import util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;


public record TaskEntry(
        String name,
        boolean started,
        boolean completed,
        List<SubTaskEntry> subtasks,
        String description,
        List<String> notes,
        LocalDateTime dueBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        UUID uuid,
        Path basePath
) {

    public TaskEntry {
        notes = notes == null ? List.of() : notes;
        subtasks = subtasks == null ? List.of() : Collections.unmodifiableList(subtasks);

    }

    public void flushToDisk() {
        if (!Files.exists(basePath)) {
            throw new IllegalStateException("Failed to update on disk, base path does not exists: " + basePath);
        }
        // Always write meta file on change
        Path metaFilePath = basePath.resolve(uuid + ".task");
        try {
            String metaJson = JsonUtils.writePretty(this);
            Files.writeString(metaFilePath, metaJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write task file: " + metaFilePath, e);
        }
    }

    public File getFile() {
        return basePath.resolve(uuid + ".task").toFile();
    }

    public TaskEntry asCompleted(LocalDateTime completedTime) {
        return new TaskEntry(
                name, started, true, subtasks, description,
                notes, dueBy, startedAt, completedTime, uuid, basePath
        );
    }

    public TaskEntry asStarted(LocalDateTime startTime) {
        return new TaskEntry(
                name, true, completed, subtasks, description,
                notes, dueBy, startTime, completedAt, uuid, basePath
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (object == null || getClass() != object.getClass()) { return false; }

        TaskEntry taskEntry = (TaskEntry) object;

        return Objects.equals(uuid, taskEntry.uuid);
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
        private String name;
        private boolean started;
        private boolean completed;
        private List<SubTaskEntry> subtasks = new ArrayList<>();
        private String description;
        private List<String> notes = new ArrayList<>();
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

        public TaskEntry build() {
            if (basePath == null) {
                Path directoryPath = Util.getEntriesPath(
                        EntryType.TODO,
                        Year.fromString(String.valueOf(LocalDateTime.now().getYear())),
                        Month.fromString(LocalDateTime.now().getMonth().toString())
                );
                if (!Files.exists(directoryPath)) {
                    throw new IllegalStateException("Could not resolve path of: " + directoryPath);
                }
                basePath = directoryPath;
            }
            return new TaskEntry(
                    name, started, completed, Collections.unmodifiableList(subtasks), description,
                    Collections.unmodifiableList(notes), dueBy, startedAt, completedAt, uuid, basePath
            );
        }
    }


}
