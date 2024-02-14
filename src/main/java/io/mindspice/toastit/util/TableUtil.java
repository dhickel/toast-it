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
                createColumn(header2, Pair::second)
        );
    }

    public static <T> String generateTable(List<T> itemsList, List<ColumnData<T>> columnList) {
        return AsciiTable.getTable(itemsList, columnList);
    }

    public static String basicBox(String content) {
        String inner = String.format("| %s |", content);
        return String.format("+%s+%n%s%n+%s+", "-".repeat(inner.length() - 2), inner, "-".repeat(inner.length() - 2));
    }

    public static <T> String generateTableWithHeader(String header, List<T> items, List<ColumnData<T>> columnData) {
        String table = generateTable(items, columnData);
        return addTableHeader(header, table);
    }

    public static String addTableHeader(String header, String table) {
        int tableLen = table.split("\n")[0].length();
        int padLen = Math.max(tableLen - 2, header.length());

        String top = String.format("+%s+", "-".repeat(padLen));
        String mid = String.format("|%s|", centerString(header, padLen));

        if (tableLen < padLen) {
            String[] split = table.split("\n");
            if (split.length == 2) {
                return String.join("\n", top, mid, top);
            } else {
                return String.join(
                        "\n",
                        top,
                        mid,
                        top,
                        Arrays.stream(split).skip(1).collect(Collectors.joining("\n")) + "\n"
                );
            }
        } else {
            return String.join("\n", top, mid, table) + "\n";
        }
    }

    public static <T> String generateIndexedPairTable(String header, String dataColumnHeader,
            List<T> items, Function<T, String> dataFunc) {

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("Index", dataColumnHeader);
        return generateTableWithHeader(
                header,
                IntStream.range(0, items.size())
                        .mapToObj(i -> Pair.of(String.valueOf(i), dataFunc.apply(items.get(i))))
                        .toList(),
                columns);
    }

    public static <T> String generateKeyPairTable(String header, List<T> items,
            Function<T, String> keyFunc, Function<T, String> valFunc) {

        List<ColumnData<Pair<String, String>>> columns = TableUtil.createKeyPairColumns("", "");
        return generateTableWithHeader(
                header,
                IntStream.range(0, items.size())
                        .mapToObj(i -> Pair.of(keyFunc.apply(items.get(i)) + i, valFunc.apply(items.get(i))))
                        .toList(),
                columns);
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

    public static String centerMultiLineString(String input, String table) {
        List<String> strings = Arrays.asList(input.split("\n"));
        return strings.stream().map(s -> centerString(s, table)).collect(Collectors.joining("\n"));
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
