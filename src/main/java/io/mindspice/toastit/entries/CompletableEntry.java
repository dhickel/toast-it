package io.mindspice.toastit.entries;

import java.time.LocalDateTime;


public interface CompletableEntry<T> {
    T asCompleted(LocalDateTime time);
    T asStarted(LocalDateTime time);
    double completionDbl();
    String completionPct();
    boolean completed();
}
