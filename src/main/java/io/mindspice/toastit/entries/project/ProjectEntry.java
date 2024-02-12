package io.mindspice.toastit.entries.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.task.Task;
import io.mindspice.toastit.enums.EntryType;
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


public record ProjectEntry(
        String name,
        boolean started,
        boolean completed,
        List<UUID> tasks,
        @JsonIgnore
        List<TaskEntry> taskObjs,
        String description,
        List<String> tags,
        Path projectPath,
        LocalDateTime dueBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<Reminder> reminders,
        UUID uuid,
        Path basePath,
        String openWith
) implements Task<ProjectEntry>, Entry {

    public ProjectEntry {
        dueBy = dueBy.truncatedTo(ChronoUnit.MINUTES);
        startedAt = startedAt.truncatedTo(ChronoUnit.MINUTES);
        completedAt = completedAt.truncatedTo(ChronoUnit.MINUTES);
        taskObjs = taskObjs == null ? List.of() : taskObjs;
        try {
            basePath = basePath == null ? Util.getEntriesPath(EntryType.PROJECT) : basePath;
        } catch (IOException e) {
            System.err.println("Error creating path for: " + this);
        }
    }

    public void loadTasks() {
        tasks.forEach(uuid -> {
//            try {
//                FullTaskEntry task = DBConnection.instance().getTask(uuid);
//                if (task != null) {
//                    taskObjs.add(task);
//                }
//            } catch (IOException e) {
//                // TODO push this to terminal
//            }
        });
    }

    public void flushToDisk() {
        if (!Files.exists(basePath)) {
            throw new IllegalStateException("Failed to update on disk, base path does not exists: " + basePath);
        }
        // Always write meta file on change
        Path metaFilePath = basePath.resolve(uuid + ".project");
        try {
            String metaJson = JSON.writePretty(this);
            Files.writeString(metaFilePath, metaJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write project file: " + metaFilePath, e);
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
                getFile().toString(),
                projectPath.toString(),
                openWith
        );
    }

    public File getFile() {
        return basePath.resolve(uuid + ".project").toFile();
    }

    @Override
    public ProjectEntry asCompleted(LocalDateTime time) {
        return new ProjectEntry(
                name, started, true, tasks, taskObjs, description, tags,
                projectPath, dueBy, startedAt, time, reminders, uuid, basePath, openWith
        );
    }

    @Override
    public ProjectEntry asStarted(LocalDateTime time) {
        return new ProjectEntry(
                name, true, completed, tasks, taskObjs, description, tags,
                projectPath, dueBy, time, completedAt, reminders, uuid, basePath, openWith
        );
    }

    @Override
    public boolean completed() {
        return taskObjs.isEmpty()
                ? completed
                : taskObjs.stream().allMatch(Task::completed);
    }

    @Override
    public double completionDbl() {
        return taskObjs.isEmpty()
                ? (completed ? 1 : 0)
                : taskObjs.stream().mapToDouble(Task::completionDbl).average().orElse(1);
    }

    @Override
    public String completionPct() {
        return Util.toPercentage(completionDbl());
    }

    @Override
    public EntryType type() {
        return EntryType.PROJECT;
    }

    @Override
    public String shortText() {
        return null;
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
        public List<UUID> tasks = new ArrayList<>();
        public List<TaskEntry> taskObjs = new ArrayList<>();
        public String description = "";
        public List<String> tags = new ArrayList<>();
        public Path projectPath = Path.of("/");
        public LocalDateTime dueBy = LocalDateTime.MAX;
        public LocalDateTime startedAt = LocalDateTime.MAX;
        public LocalDateTime completedAt = LocalDateTime.MAX;
        public List<Reminder> reminders;
        public UUID uuid = UUID.randomUUID();
        public Path basePath;
        public String openWith = "";

        public Builder() { }

        public Builder(ProjectEntry p) {
            this.name = p.name;
            this.started = p.started;
            this.completed = p.completed;
            this.tasks = p.tasks;
            this.taskObjs = p.taskObjs;
            this.description = p.description;
            this.tags = p.tags;
            this.projectPath = p.projectPath;
            this.dueBy = p.dueBy;
            this.startedAt = p.startedAt;
            this.completedAt = p.completedAt;
            this.reminders = p.reminders;
            this.uuid = p.uuid;
            this.basePath = p.basePath;
            this.openWith = p.openWith;
        }

        public ProjectEntry build() throws IOException {
            if (basePath == null) {
                basePath = Util.getEntriesPath(EntryType.PROJECT);
            }
            return new ProjectEntry(
                    name,
                    started,
                    completed,
                    Collections.unmodifiableList(tasks),
                    Collections.unmodifiableList(taskObjs),
                    description,
                    Collections.unmodifiableList(tags),
                    projectPath,
                    dueBy,
                    startedAt,
                    completedAt,
                    reminders,
                    uuid,
                    basePath,
                    openWith
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
            String metaPath,
            String projectPath,
            String openWith
    ) { }

}
