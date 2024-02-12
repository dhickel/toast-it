package io.mindspice.toastit.entries.task;

import java.time.LocalDateTime;


public interface Task<T> {
    T asCompleted(LocalDateTime time);
    T asStarted(LocalDateTime time);
    double completionDbl();
    String completionPct();
    boolean completed();
}
