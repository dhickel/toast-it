package util;

import com.github.freva.asciitable.*;
import gnu.lists.Strings;

import java.util.List;
import java.util.function.Function;


public class TableUtil {

    public static <T> ColumnData<T> createColumn(String header, Function<T, String> printFunc) {
        return new Column()
                .maxWidth(Settings.TABLE_MAX_COLUMN_WIDTH, OverflowBehaviour.ELLIPSIS_RIGHT)
                .header(header)
                .headerAlign(HorizontalAlign.CENTER)
                .with(printFunc);
    }

    public static <T> String generateTable(List<T> itemsList, List<ColumnData<T>> columnList) {
        return AsciiTable.getTable(itemsList, columnList);
    }

    public static <T> String generateTableWithTitle(String title, List<T> itemsList, List<ColumnData<T>> columnList) {
        String table = AsciiTable.getTable(itemsList, columnList);
        String centeredTitle = centerString(title, table);
        return String.join("\n", centeredTitle, table);
    }

    public static String centerString(String input, int length) {
        if (input.length() >= length) {
            return input;
        }

        int totalPadding = length - input.length();
        int paddingStart = totalPadding / 2;
        int paddingEnd = totalPadding - paddingStart;

        return " ".repeat(paddingStart) + input + " ".repeat(paddingEnd);
    }

    public static String centerString(String input, String table) {
        int length = table.split("\n")[0].length();
        if (input.length() >= length) {
            return input;
        }

        int totalPadding = length - input.length();
        int paddingStart = totalPadding / 2;
        int paddingEnd = totalPadding - paddingStart;

        return " ".repeat(paddingStart) + input + " ".repeat(paddingEnd);
    }

}
