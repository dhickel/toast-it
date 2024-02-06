package todo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public record SubTaskEntry(
        String name,
        String description,
        List<SubTaskEntry> subTasks,
        boolean completed,
        LocalDateTime completedAt
) {

    public SubTaskEntry withSubTask(SubTaskEntry subTask) {
        List<SubTaskEntry> newSubtasks = new ArrayList<>(subTasks);
        newSubtasks.add(subTask);
        newSubtasks = Collections.unmodifiableList(newSubtasks);
        return new SubTaskEntry(name, description, newSubtasks, completed, completedAt);
    }

    public SubTaskEntry asCompleted(LocalDateTime time) {
        return new SubTaskEntry(name, description, subTasks, true, time);
    }

    public String completionPct() {
        return util.Util.toPercentage(completionDbl());
    }

    public double completionDbl() {
        return subTasks.isEmpty()
                ? (completed ? 1 : 0)
                : subTasks.stream().mapToDouble(SubTaskEntry::completionDbl).average().orElse(1);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private List<SubTaskEntry> subTasks = new ArrayList<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addSubTask(SubTaskEntry subTask) {
            subTasks.add(subTask);
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
