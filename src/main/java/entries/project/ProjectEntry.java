package entries.project;

import application.Database;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import entries.task.FullTaskEntry;
import entries.task.TaskEntry;
import enums.EntryType;
import util.JSON;
import util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


public record ProjectEntry(
        String name,
        boolean started,
        boolean completed,
        List<UUID> tasks,
        @JsonIgnore
        List<FullTaskEntry> taskObjs,
        String description,
        Set<String> tags,
        Path projectPath,
        LocalDateTime dueBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        UUID uuid,
        Path basePath,
        String openWith
) implements TaskEntry<ProjectEntry> {

    public void loadTasks() {
        tasks.forEach(uuid -> {
            try {
                FullTaskEntry task = Database.instance().getTask(uuid);
                if (task != null) {
                    taskObjs.add(task);
                }
            } catch (IOException e) {
                // TODO push this to terminal
            }
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
                : taskObjs.stream().allMatch(TaskEntry::completed);
    }

    @Override
    public double completionDbl() {
        return taskObjs.isEmpty()
                ? (completed ? 1 : 0)
                : taskObjs.stream().mapToDouble(TaskEntry::completionDbl).average().orElse(1);
    }

    @Override
    public String completionPct() {
        return util.Util.toPercentage(completionDbl());
    }

    @Override
    public String terminalText() {
        return null;
    }

    public static class Builder {
        private String name = "Unnamed";
        private boolean started = false;
        private boolean completed = false;
        private List<UUID> tasks = new ArrayList<>();
        private List<FullTaskEntry> taskObjs = new ArrayList<>();
        private String description = "";
        private Set<String> tags = new HashSet<>();
        private Path projectDirectory = Path.of("/");
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
            this.projectDirectory = p.projectPath;
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

        public Builder setDescription(String description) {
            this.description = description;
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

        public Builder setProjectDirectory(Path projectDirectory) {
            this.projectDirectory = projectDirectory;
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

        public ProjectEntry build() {
            if (basePath == null) {
                Path directoryPath = Util.getEntriesPath(
                        EntryType.PROJECT,
                        LocalDateTime.now().getYear(),
                        LocalDateTime.now().getMonth()
                );
                if (!Files.exists(directoryPath)) {
                    throw new IllegalStateException("Could not resolve path of: " + directoryPath);
                }
                basePath = directoryPath;
            }
            return new ProjectEntry(
                    name,
                    started,
                    completed,
                    Collections.unmodifiableList(tasks),
                    Collections.unmodifiableList(taskObjs),
                    description,
                    Collections.unmodifiableSet(tags),
                    projectDirectory,
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
