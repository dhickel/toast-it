package io.mindspice.toastit.entries;

import java.time.LocalDateTime;


public interface DatedEntry extends Entry {
    LocalDateTime startedAt();

    LocalDateTime dueBy();

    LocalDateTime completedAt();

    boolean completed();
}
