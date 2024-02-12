package io.mindspice.toastit.notification;

import io.mindspice.toastit.enums.NotificationLevel;
import io.mindspice.toastit.util.DateTimeUtil;
import org.mockito.internal.matchers.Not;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public record Reminder(
        LocalDateTime time,
        NotificationLevel level
) {

    public Reminder {
        time = time.truncatedTo(ChronoUnit.MINUTES);
    }

    public Stub getStub() {
        return new Stub(
                DateTimeUtil.localToUnix(time),
                level
        );
    }

    public String toString() {
        return String.format("%s | %s", DateTimeUtil.printDateTimeShort(time), level);
    }

    public record Stub(
            long time,
            NotificationLevel level
    ) {
        public Reminder asFull() {
            return new Reminder(
                    DateTimeUtil.unixToLocal(time),
                    level
            );
        }
    }
}
