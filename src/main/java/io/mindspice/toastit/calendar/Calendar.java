package io.mindspice.toastit.calendar;

import com.indvd00m.ascii.render.Render;
import com.indvd00m.ascii.render.api.ICanvas;
import com.indvd00m.ascii.render.api.IContextBuilder;
import com.indvd00m.ascii.render.api.IRender;
import com.indvd00m.ascii.render.elements.Label;
import com.indvd00m.ascii.render.elements.Table;
import com.indvd00m.ascii.render.elements.Text;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.util.Settings;
import io.mindspice.toastit.util.TableUtil;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;


public class Calendar {
    public static Function<CalendarCell, Text> cellFormatter;

    public Function<CalendarCell, Text> cellFormatter() {
        return cellFormatter;
    }

    public void setCellFormatter(Function<CalendarCell, Text> cellFormatter) {
        Calendar.cellFormatter = cellFormatter;
    }

    public static String generateCalendar(int year, Month month, int width, int height, UnaryOperator<CalendarCell> mapper) {
        var cellInfo = generateCalenderCells(YearMonth.of(year, month));
        var header = createHeaderRow(width);
        var table = createDayCells(cellInfo.first(), width, height, cellInfo.second(), mapper);
        return header + "\n" + table;
    }

    public static String createHeaderRow(int width) {
        IRender render = new Render();
        IContextBuilder builder = render.newBuilder();
        builder.width(width * 7).height(3);
        Table table = new Table(7, 1);
        IntStream.range(1, 8).forEach(i ->
                table.setElement(i, 1, new Label(TableUtil.centerString(DayOfWeek.values()[i - 1].name(), width))
                ));
        builder.element(table);
        ICanvas canvas = render.render(builder.build());
        return canvas.getText();
    }

    public static Pair<Integer, List<CalendarCell>> generateCalenderCells(YearMonth yearMonth) {
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.get(ChronoField.DAY_OF_WEEK); // Make Monday = 1 through Sunday = 7
        int daysInMonth = yearMonth.lengthOfMonth();
        int row = 1; // ASCII tables indexes from 1

        List<CalendarCell> cellList = new ArrayList<>(daysInMonth);
        for (int day = 1; day <= daysInMonth; day++) {
            int column = (day + firstDayOfWeek - 2) % 7 + 1; // Adjust for ASCII table indexing
            if ((day + firstDayOfWeek - 2) >= 7 && column == 1) {
                row++;
            }
            cellList.add(CalendarCell.of(
                    day, column, row, LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day))
            );
        }
        int rowsNeeded = row;

        return Pair.of(rowsNeeded, Collections.unmodifiableList(cellList));
    }

    public static String createDayCells(int rowsNeeded, int width, int height, List<CalendarCell> values,
            UnaryOperator<CalendarCell> cellMapper) {
        IRender render = new Render();
        IContextBuilder builder = render.newBuilder();
        builder.width(width * 7).height(height * rowsNeeded);
        Table table = new Table(7, rowsNeeded);

        List<CalendarCell> cellValues = cellMapper == null ? values : values.stream().map(cellMapper).toList();

        cellValues.forEach(c -> {
            table.setElement(
                    c.col(),
                    c.row(),
                    cellFormatter == null ? c.asCellText() : c.asCellTextFormatted(cellFormatter)
            );
            if (c.isHighlighted()) {
                table.setHighlighted(c.col(), c.row(), true);
            }
        });

        builder.element(table);
        ICanvas canvas = render.render(builder.build());
        return canvas.getText();
    }
}
