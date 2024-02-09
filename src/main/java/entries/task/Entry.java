package entries.task;

import java.time.LocalDateTime;


public interface Entry<T> {
    T asCompleted(LocalDateTime time);
    T asStarted(LocalDateTime time);
    double completionDbl();
    String completionPct();
    boolean completed();
    String terminalText();
}
