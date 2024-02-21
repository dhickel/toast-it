package io.mindspice.toastit.calendar;

import com.indvd00m.ascii.render.elements.Text;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;


public record CalendarCell(
        int day,
        int col,
        int row,
        LocalDate date,
        boolean isHighlighted,
        List<String> items,
        List<String> itemUUIDs
) {
    public static CalendarCell of(int day, int col, int row, LocalDate date) {
        return new CalendarCell(day, col, row, date, false, List.of(), List.of());
    }

    public CalendarCell withItems(List<String> items) {
        return new CalendarCell(day, col, row, date, isHighlighted, Collections.unmodifiableList(items), itemUUIDs);
    }

    public CalendarCell withItemUUIDs(List<String> uuids) {
        return new CalendarCell(day, col, row, date, isHighlighted, items, Collections.unmodifiableList(uuids));
    }

    public CalendarCell withItemsAndUUIDs(List<String> items, List<String> uuids) {
        return new CalendarCell(day, col, row, date, isHighlighted,
                Collections.unmodifiableList(items), Collections.unmodifiableList(uuids)
        );
    }

    public CalendarCell asHighlighted() {
        return new CalendarCell(day, col, row, date, true, items, itemUUIDs);
    }

    public Text asCellText() {
        StringBuilder builder = new StringBuilder(" ").append(day);
        if (items != null) {
            items.forEach(item -> builder.append("\n ").append(item));
        }
        return new Text(builder.toString());
    }

    public Text asCellTextFormatted(Function<CalendarCell, Text> formatFunction) {
        return formatFunction.apply(this);
    }
}
