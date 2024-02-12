package io.mindspice.toastit.entries.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.Util;

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
        List<SubTask> subtasks,
        String description,
        List<String> notes,
        List<String> tags,
        LocalDateTime dueBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<Reminder> reminders,
        UUID uuid,
        Path basePath
) implements Task<TaskEntry>, Entry {

    public TaskEntry {
        dueBy = dueBy.truncatedTo(ChronoUnit.MINUTES);
        startedAt = startedAt.truncatedTo(ChronoUnit.MINUTES);
        completedAt = completedAt.truncatedTo(ChronoUnit.MINUTES);
        try {
            basePath = basePath == null ? Util.getEntriesPath(EntryType.TASK) : basePath;
        } catch (IOException e) {
            System.err.println("Error creating path for: " + this);
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
                JSON.writeString(reminders.stream().map(Reminder::getStub).toList()),
                getFile().toString()
        );
    }

    public File getFile() {
        return basePath.resolve(uuid + ".task").toFile();
    }

    public TaskEntry asCompleted(LocalDateTime completedTime) {
        return new TaskEntry(
                name, started, true, subtasks, description, notes,
                tags, dueBy, startedAt, completedTime, reminders, uuid, basePath
        );
    }

    public TaskEntry asStarted(LocalDateTime startTime) {
        return new TaskEntry(
                name, true, completed, subtasks, description, notes,
                tags, dueBy, startTime, completedAt, reminders, uuid, basePath
        );
    }

    @Override
    public boolean completed() {
        return subtasks.isEmpty()
                ? completed
                : subtasks.stream().allMatch(Task::completed);
    }

    @Override
    public double completionDbl() {
        return subtasks.isEmpty()
                ? (completed ? 1 : 0)
                : subtasks.stream().mapToDouble(Task::completionDbl).average().orElse(1);
    }

    @Override
    public String completionPct() {
        return Util.toPercentage(completionDbl());
    }

    @Override
    public EntryType type() {
        return EntryType.TASK;
    }

    @Override
    public String shortText() {
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
        public String name = "Unnamed";
        public boolean started = false;
        public boolean completed = false;
        public List<SubTask> subtasks = new ArrayList<>();
        public String description = "";
        public List<String> notes = new ArrayList<>();
        public List<String> tags = new ArrayList<>();
        public LocalDateTime dueBy = LocalDateTime.MAX;
        public LocalDateTime startedAt = LocalDateTime.MAX;
        public LocalDateTime completedAt = LocalDateTime.MAX;
        public List<Reminder> reminders;

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
            this.reminders = t.reminders;
            this.uuid = t.uuid;
            this.basePath = t.basePath;
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
                    Collections.unmodifiableList(tags),
                    dueBy,
                    startedAt,
                    completedAt,
                    reminders,
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
            String reminders,
            String metaPath
    ) { }
}
