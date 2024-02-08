package entries.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public record SubTaskEntry(
        String name,
        String description,
        List<SubTaskEntry> subtasks,
        boolean completed,
        LocalDateTime completedAt
) implements TaskEntry<SubTaskEntry> {

    public SubTaskEntry asCompleted(LocalDateTime time) {
        return new SubTaskEntry(name, description, subtasks, true, time);
    }

    @Override
    public SubTaskEntry asStarted(LocalDateTime time) {
        return this;
    }

    @Override
    public String completionPct() {
        return util.Util.toPercentage(completionDbl());
    }

    @Override
    public double completionDbl() {
        return subtasks.isEmpty()
                ? (completed ? 1 : 0)
                : subtasks.stream().mapToDouble(SubTaskEntry::completionDbl).average().orElse(1);
    }

    @Override
    public String terminalText() {
        return null;
    }

    @Override
    public boolean completed() {
        return subtasks.isEmpty()
                ? completed
                : subtasks.stream().allMatch(TaskEntry::completed);
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
        private List<SubTaskEntry> subTasks = new ArrayList<>();
        private boolean completed = false;
        private LocalDateTime completedAt = LocalDateTime.MAX;

        public Builder() { }

        public Builder(SubTaskEntry t) {
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

        public Builder setSubTasks(List<SubTaskEntry> subTasks) {
            this.subTasks = subTasks;
            return this;
        }

        public Builder add(SubTaskEntry subTask) {
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

        public SubTaskEntry build() {
            return new SubTaskEntry(
                    name,
                    description,
                    Collections.unmodifiableList(subTasks),
                    false,
                    LocalDateTime.MAX
            );
        }
    }
}
