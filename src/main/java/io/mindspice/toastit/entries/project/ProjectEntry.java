package io.mindspice.toastit.entries.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.DatedEntry;
import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.CompletableEntry;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.JSON;
import io.mindspice.toastit.util.TableUtil;
import io.mindspice.toastit.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;


public record ProjectEntry(
        String name,
        boolean started,
        boolean completed,
        List<UUID> tasks,
        @JsonIgnore
        List<TaskEntry> taskObjs,
        String description,
        List<String> notes,
        List<String> tags,
        Path projectPath,
        LocalDateTime dueBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<Reminder> reminders,
        UUID uuid,
        Path basePath,
        String openWith
) implements CompletableEntry<ProjectEntry>, DatedEntry, Entry {

    public ProjectEntry {
        dueBy = dueBy.truncatedTo(ChronoUnit.MINUTES);
        startedAt = startedAt.truncatedTo(ChronoUnit.MINUTES);
        completedAt = completedAt.truncatedTo(ChronoUnit.MINUTES);

        if ((taskObjs == null || taskObjs.isEmpty()) & !tasks.isEmpty()) { ;
            taskObjs = Collections.unmodifiableList(loadTasks(tasks));
        } else if (tasks.isEmpty()) {
            tasks = List.of();
            taskObjs = List.of();
        }
        try {
            basePath = basePath == null ? Util.getEntriesPath(EntryType.PROJECT) : basePath;
        } catch (IOException e) {
            System.err.println("Error creating path for: " + this);
        }
    }

    public List<TaskEntry> loadTasks(List<UUID> taskUUIDs) {
        if (taskUUIDs == null) {
            return List.of();
        }
        List<TaskEntry> loadedTasks = new ArrayList<>();
        taskUUIDs.forEach(uuid -> {
            try {
                TaskEntry task = App.instance().getDatabase().getTaskByUUID(uuid);
                if (task != null) {
                    loadedTasks.add(task);
                } else {
                    System.err.printf("Task: %s null for project: %s", uuid, this.uuid);
                }
            } catch (IOException e) {
                System.err.println("Error lading task" + e);
            }
        });
        return loadedTasks;
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
                name, started, true, tasks, taskObjs, description, notes, tags, projectPath,
                dueBy, startedAt, time, reminders, uuid, basePath, openWith
        );
    }

    @Override
    public ProjectEntry asStarted(LocalDateTime time) {
        return new ProjectEntry(
                name, true, completed, tasks, taskObjs, description, notes, tags, projectPath,
                dueBy, time, completedAt, reminders, uuid, basePath, openWith
        );
    }

    @Override
    public boolean completed() {
        return taskObjs.isEmpty()
                ? completed
                : taskObjs.stream().allMatch(CompletableEntry::completed);
    }

    @Override
    public double completionDbl() {
        return taskObjs.isEmpty()
                ? (completed ? 1 : 0)
                : taskObjs.stream().mapToDouble(CompletableEntry::completionDbl).average().orElse(1);
    }

    @Override
    public String completionPct() {
        return Util.toPercentage(completionDbl());
    }

    @Override
    public EntryType type() {
        return EntryType.PROJECT;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        public String name = "";
        public boolean started = false;
        public boolean completed = false;
        public List<UUID> tasks = new ArrayList<>();
        public List<TaskEntry> taskObjs = new ArrayList<>(2);
        public String description = "";
        public List<String> notes = new ArrayList<>(2);
        public List<String> tags = new ArrayList<>(2);
        public Path projectPath = Path.of("/");
        public LocalDateTime dueBy = DateTimeUtil.MAX;
        public LocalDateTime startedAt = DateTimeUtil.MAX;
        public LocalDateTime completedAt = DateTimeUtil.MAX;
        public List<Reminder> reminders = new ArrayList<>(2);
        public UUID uuid = UUID.randomUUID();
        public Path basePath;
        public String openWith = "";

        public Builder() { }

        public Builder(ProjectEntry p) {
            this.name = p.name;
            this.started = p.started;
            this.completed = p.completed;
            this.tasks = new ArrayList<>(p.tasks);
            this.taskObjs = new ArrayList<>(p.taskObjs);
            this.description = p.description;
            this.notes = p.notes;
            this.tags = new ArrayList<>(p.tags);
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
                    notes,
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

        public List<Pair<String, String>> toTableState() {
            List<Pair<String, String>> rntList = new ArrayList<>();
            if (!name.isEmpty()) {
                rntList.add(Pair.of("Name", name));
            }
            if (!description.isEmpty()) {
                rntList.add(Pair.of("Description", TableUtil.truncateString(description)));
            }
            if (!tags.isEmpty()) {
                rntList.add(Pair.of("Tags", tags.toString()));
            }
            if (!taskObjs.isEmpty()) {
                IntStream.range(0, taskObjs.size()).forEach(i -> rntList.add(
                        Pair.of(String.format("Task %d", i + 1), taskObjs.get(i).name())
                ));
            }
            if (!notes.isEmpty()) {
                IntStream.range(0, notes.size()).forEach(i -> rntList.add(
                        Pair.of(String.format("Note %d", i + 1), TableUtil.truncateString(notes.get(i)))
                ));
            }
            if (!projectPath.toString().equals("/")) {
                rntList.add(Pair.of("Project Path", projectPath.toString()));
            }
            rntList.add(Pair.of("Started", String.valueOf(started)));
            if (started) {
                rntList.add(Pair.of("Started At", DateTimeUtil.printDateTimeFull(startedAt)));

            }
            if (!dueBy.equals(DateTimeUtil.MAX)) {
                rntList.add(Pair.of("Due By", DateTimeUtil.printDateTimeFull(dueBy)));
            }
            rntList.add(Pair.of("Completed", String.valueOf(completed)));
            if (completed) {
                rntList.add(Pair.of("Completed At", DateTimeUtil.printDateTimeFull(completedAt)));
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
