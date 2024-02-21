package io.mindspice.toastit.shell.evaluators;

import io.mindspice.toastit.calendar.Calendar;
import io.mindspice.toastit.util.Settings;

import java.time.Instant;
import java.time.LocalDate;


public class CalendarEval extends ShellEvaluator<CalendarEval> {
    public String calendar;
    public long lastTime = 0;

    public CalendarEval() {
    }

    @Override
    public String modeDisplay() {
        if (lastTime + Settings.CALENDAR_REFRESH_SEC < Instant.now().getEpochSecond()) {
            lastTime = Instant.now().getEpochSecond();
            LocalDate date = LocalDate.now();
            calendar = Calendar.generateCalendar(
                    date.getYear(),
                    date.getMonth(),
                    Settings.CALENDAR_CELL_WIDTH,
                    Settings.CALENDAR_CELL_HEIGHT,
                    Settings.CALENDAR_CELL_MAPPER
            );
        }
        return calendar + "\n";
    }
}
