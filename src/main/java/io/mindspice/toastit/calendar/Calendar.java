package io.mindspice.toastit.calendar;

import com.indvd00m.ascii.render.Render;
import com.indvd00m.ascii.render.api.ICanvas;
import com.indvd00m.ascii.render.api.IContextBuilder;
import com.indvd00m.ascii.render.api.IRender;
import com.indvd00m.ascii.render.elements.Label;
import com.indvd00m.ascii.render.elements.Table;
import com.indvd00m.ascii.render.elements.Text;
import io.mindspice.toastit.util.Settings;

import java.time.DayOfWeek;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;


public class Calendar {
    public static Function<CalendarCell, CalendarCell> cellMapper;
    public static Function<CalendarCell, Text> cellFormatter;

    public Function<CalendarCell, CalendarCell> cellMapper() {
        return cellMapper;
    }

    public void setCellMapper(Function<CalendarCell, CalendarCell> cellMapper) {
        this.cellMapper = cellMapper;
    }

    public Function<CalendarCell, Text> cellFormatter() {
        return cellFormatter;
    }

    public void setCellFormatter(Function<CalendarCell, Text> cellFormatter) {
        Calendar.cellFormatter = cellFormatter;
    }

    private String createHeaderRow() {
        IRender render = new Render();
        IContextBuilder builder = render.newBuilder();
        builder.width(Settings.CALENDER_CELL_WIDTH * 7).height(Settings.CALENDER_HEADER_HEIGHT);
        Table table = new Table(7, 1);
        String spacing = " ".repeat(Settings.CALENDAR_HEADER_LEADING_SPACES);
        IntStream.range(1, 8).forEach(i ->
                table.setElement(i, 1, new Label(spacing + DayOfWeek.values()[i - 1].name()))
        );
        builder.element(table);
        ICanvas canvas = render.render(builder.build());
        return canvas.getText();
    }

    private String createDayCells(int rowsNeeded, List<CalendarCell> values) {
        IRender render = new Render();
        IContextBuilder builder = render.newBuilder();
        builder.width(Settings.CALENDER_CELL_WIDTH * 7).height(Settings.CALENDER_CELL_HEIGHT * rowsNeeded);
        Table table = new Table(7, rowsNeeded);

        List<CalendarCell> cellValues = cellMapper == null
                ? values
                : values.stream().map(v -> cellMapper.apply(v)).toList();

        cellValues.forEach(c -> {
            table.setElement(c.col(), c.row(), cellFormatter == null
                    ? c.asCellText()
                    : c.asCellTextFormatted(cellFormatter));
            if (c.isHighlighted()) {
                table.setHighlighted(c.col(), c.row(), true);
            }
        });

        builder.element(table);
        ICanvas canvas = render.render(builder.build());
        return canvas.getText();
    }
}
