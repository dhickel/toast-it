package io.mindspice.toastit.util;

import com.github.freva.asciitable.*;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class TableUtil {

    public static <T> ColumnData<T> createColumn(String header, Function<T, String> printFunc) {
        var col = new Column()
                .maxWidth(Settings.TABLE_MAX_COLUMN_WIDTH, OverflowBehaviour.ELLIPSIS_RIGHT);
        if (!header.isEmpty()) {
            col.header(header).headerAlign(HorizontalAlign.CENTER);
        }
        return col.dataAlign(Settings.TABLE_DEFAULT_ALIGNMENT).with(printFunc);
    }

    public static <T> ColumnData<T> createColumn(String header, Function<T, String> printFunc, int maxWidth, int minWidth) {
        var col = new Column()
                .maxWidth(maxWidth, OverflowBehaviour.ELLIPSIS_RIGHT)
                .minWidth(minWidth);
        if (!header.isEmpty()) {
            col.header(header).headerAlign(HorizontalAlign.CENTER);
        }
        return col.dataAlign(Settings.TABLE_DEFAULT_ALIGNMENT).with(printFunc);
    }

    public static List<ColumnData<Pair<String, String>>> createKeyPairColumns(String header1, String header2) {
        return List.of(
                createColumn(header1, Pair::first),
                createColumn(header1, Pair::second)
        );
    }

    public static <T> String generateTable(List<T> itemsList, List<ColumnData<T>> columnList) {
        return AsciiTable.getTable(itemsList, columnList);
    }

    public static String basicBox(String content) {
        String inner = String.format("| %s |", content);
        return String.format("+%s+%n%s%n+%s+", "-".repeat(inner.length() - 2), inner, "-".repeat(inner.length() - 2));
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

    public static String mergeAndPadTable(int horizontalPad, String... tables) {
        List<List<String>> splitRows = new ArrayList<>();
        for (String table : tables) {
            splitRows.add(Arrays.asList(table.split("\n")));
        }
        return mergeAndPadRowList(splitRows, " ".repeat(horizontalPad));
    }

    public static String mergeAndPadRowList(List<List<String>> lists, String horizontalPad) {
        // Determine maximum size among all lists
        int maxSize = lists.stream().mapToInt(List::size).max().orElse(0);

        // Pad lists to have the same size with appropriate spaces
        lists.forEach(list -> {
            int initialSize = list.size();
            for (int i = initialSize; i < maxSize; i++) {
                list.add(" ".repeat(initialSize > 0 ? list.getFirst().length() : 0));
            }
        });

        // Concatenate and add horizontal padding
        return IntStream.range(0, maxSize)
                .mapToObj(rowIndex -> lists.stream()
                        .map(list -> list.get(rowIndex))
                        .collect(Collectors.joining(horizontalPad)))
                .collect(Collectors.joining("\n"));
    }

}
