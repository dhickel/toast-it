package io.mindspice.toastit.notification;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;


public record ScheduledNotification(
        UUID uuid,
        LocalDateTime time,
        ScheduledFuture<?> scheduledFuture
) {

    public void cancel() {
        scheduledFuture.cancel(false);
    }
}
