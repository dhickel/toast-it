package io.mindspice.toastit.entries.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.task.Task;
import io.mindspice.toastit.enums.EntryType;
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
                projectPath, dueBy, startedAt, time, uuid, basePath, openWith
        );
    }

    @Override
    public ProjectEntry asStarted(LocalDateTime time) {
        return new ProjectEntry(
                name, true, completed, tasks, taskObjs, description, tags,
                projectPath, dueBy, time, completedAt, uuid, basePath, openWith
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
        private String name = "Unnamed";
        private boolean started = false;
        private boolean completed = false;
        private List<UUID> tasks = new ArrayList<>();
        private List<TaskEntry> taskObjs = new ArrayList<>();
        private String description = "";
        private List<String> tags = new ArrayList<>();
        private Path projectPath = Path.of("/");
        private LocalDateTime dueBy = LocalDateTime.MAX;
        private LocalDateTime startedAt = LocalDateTime.MAX;
        private LocalDateTime completedAt = LocalDateTime.MAX;
        private UUID uuid = UUID.randomUUID();
        private Path basePath;
        private String openWith = "";

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
            this.uuid = p.uuid;
            this.basePath = p.basePath;
            this.openWith = p.openWith;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }

        public boolean started() {
            return started;
        }

        public Builder setTasks(List<UUID> tasks) {
            this.tasks = tasks;
            return this;
        }

        public Builder addTask(UUID task) {
            this.tasks.add(task);
            return this;
        }

        public Builder removeTask(UUID task) {
            this.tasks.remove(task);
            return this;
        }

        public Builder setTaskObjs(List<TaskEntry> tasks){
            this.taskObjs = tasks;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
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

        public Builder setProjectDirectory(Path projectDirectory) {
            this.projectPath = projectDirectory;
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

        public Builder setBasePath(Path basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder setOpenWith(String openWith) {
            this.openWith = openWith;
            return this;
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
            String metaPath,
            String projectPath,
            String openWith
    ) { }

}
