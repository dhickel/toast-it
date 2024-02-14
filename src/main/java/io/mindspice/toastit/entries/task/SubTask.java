package io.mindspice.toastit.entries.task;

import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.util.Util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public record SubTask(
        String name,
        String description,
        boolean completed,
        LocalDateTime completedAt
) implements Task<SubTask> {

    public SubTask {
        completedAt = completedAt.truncatedTo(ChronoUnit.MINUTES);
    }

    public SubTask asCompleted(LocalDateTime time) {
        return new SubTask(name, description, true, time);
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
        return completed ? 1 : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder updateBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        public String name = "";
        public String description = "";
        public boolean completed = false;
        public LocalDateTime completedAt = LocalDateTime.MAX;

        public Builder() { }

        public Builder(SubTask t) {
            this.name = t.name;
            this.description = t.description;
            this.completed = t.completed;
            this.completedAt = t.completedAt;
        }

        public SubTask build() {
            return new SubTask(
                    name,
                    description,
                    false,
                    LocalDateTime.MAX
            );
        }

        public List<Pair<String, String>> toTableState() {
            return List.of(
                    Pair.of("name", name),
                    Pair.of("Description", description)
            );
        }
    }
}
