package io.mindspice.toastit.entries;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;


public interface CalendarEvents {
   List<String> getCalendarEvents(LocalDate date, Function<DatedEntry, String> dataMapper);

}
