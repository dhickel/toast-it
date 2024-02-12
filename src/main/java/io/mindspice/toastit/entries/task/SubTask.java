package io.mindspice.toastit.entries.task;

import io.mindspice.toastit.util.Util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public record SubTask(
        String name,
        String description,
        List<SubTask> subtasks,
        boolean completed,
        LocalDateTime completedAt
) implements Task<SubTask> {

    public SubTask {
        completedAt = completedAt.truncatedTo(ChronoUnit.MINUTES);
    }

    public SubTask asCompleted(LocalDateTime time) {
        return new SubTask(name, description, subtasks, true, time);
    }

    @Override
    public SubTask asStarted(LocalDateTime time) {
        return this;
    }

    @Override
    public String completionPct() {
        return Util.toPercentage(completionDbl());
    }

    @Override
    public double completionDbl() {
        return subtasks.isEmpty()
                ? (completed ? 1 : 0)
                : subtasks.stream().mapToDouble(SubTask::completionDbl).average().orElse(1);
    }

    @Override
    public boolean completed() {
        return subtasks.isEmpty()
                ? completed
                : subtasks.stream().allMatch(Task::completed);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String name = "Unnamed";
        private String description = "";
        private List<SubTask> subTasks = new ArrayList<>();
        private boolean completed = false;
        private LocalDateTime completedAt = LocalDateTime.MAX;

        public Builder() { }

        public Builder(SubTask t) {
            this.name = t.name;
            this.description = t.description;
            this.subTasks = t.subtasks;
            this.completed = t.completed;
            this.completedAt = t.completedAt;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setSubTasks(List<SubTask> subTasks) {
            this.subTasks = subTasks;
            return this;
        }

        public Builder add(SubTask subTask) {
            this.subTasks.add(subTask);
            return this;
        }

        public Builder setCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public SubTask build() {
            return new SubTask(
                    name,
                    description,
                    Collections.unmodifiableList(subTasks),
                    false,
                    LocalDateTime.MAX
            );
        }
    }
}
