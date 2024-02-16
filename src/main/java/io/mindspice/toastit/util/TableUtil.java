package io.mindspice.toastit.util;

import com.github.freva.asciitable.*;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static kawa.lib.prim_imports.string;


public class TableUtil {

    public static <T> ColumnData<T> createColumn(String header, Function<T, String> printFunc) {
        var col = new Column()
                .maxWidth(Settings.TABLE_MAX_COLUMN_WIDTH, Settings.TABLE_OVERFLOW_BEHAVIOR);
        if (!header.isEmpty()) {
            col.header(header).headerAlign(HorizontalAlign.CENTER);
        }
        return col.dataAlign(Settings.TABLE_DEFAULT_ALIGNMENT).with(printFunc);
    }

    public static <T> ColumnData<T> createColumn(String header, Function<T, String> printFunc, int minWidth, int maxWidth) {
        var col = new Column()
                .minWidth(Math.min(minWidth, maxWidth))
                .maxWidth(maxWidth,  Settings.TABLE_OVERFLOW_BEHAVIOR);

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

    public static String basicRow(int offset, String ... items) {
        StringBuilder top = new StringBuilder(" ".repeat(offset) + "+");
        StringBuilder mid = new StringBuilder(" ".repeat(offset) +"|");
        StringBuilder bottom = new StringBuilder(" ".repeat(offset) +"+");
        for (var s : items) {
            int len = s.length();
            top.append("-".repeat(len + 2)).append("+");
            mid.append(" ").append(s).append(" |");
            bottom.append("-".repeat(len + 2)).append("+");
        }

        return String.join("\n", top, mid, bottom);
    }

    public static <T> String generateTable(List<T> itemsList, List<ColumnData<T>> columnList) {
        return AsciiTable.getTable(TableConfig.BORDER, itemsList, columnList);
    }

    public static <T> String generateTableWithHeader(String header, List<T> items, List<ColumnData<T>> columnData) {
        String table = generateTable(items, columnData);
        return addTableHeader(header, table);
    }

    public static String addTableHeader(String header, String table) {
        String[] splitTable = table.split("\n");
        int tableLen = splitTable[0].length();
        int padLen = Math.max(tableLen - 2, header.length());

        Character[] b = TableConfig.BORDER;
        String top = String.format("%s%s%s", b[0].toString(), b[1].toString().repeat(padLen), b[3].toString());
        String mid = String.format("%s%s%s", b[4].toString(), centerString(header, padLen), b[4].toString());
        splitTable[0] = String.format("%s%s%s", b[7], splitTable[0].substring(1, tableLen - 1), b[10]);
        if (tableLen < padLen) {
            if (splitTable.length == 2) {
                return String.join("\n", top, mid, top);
            } else {
                return String.join(
                        "\n",
                        top,
                        mid,
                        top,
                        Arrays.stream(splitTable).skip(1).collect(Collectors.joining("\n")) + "\n"
                );
            }
        } else {
            return String.join("\n", top, mid, String.join("\n", splitTable)) + "\n";
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
            splitRows.add(new ArrayList<>(Arrays.asList(table.split("\n"))));
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

    public static String wrapString(String input, int maxLength) {
        String[] lines = input.split("\n");
        List<String> result = new ArrayList<>();

        for (String line : lines) {
            processLine(line, maxLength, result);
        }
        return String.join("\n", result);
    }

    private static void processLine(String line, int maxLength, List<String> result) {
        if (line.length() <= maxLength) {
            result.add(line);
            return;
        }
        int lastSpace = -1;
        for (int i = 0; i < maxLength; i++) {
            if (line.charAt(i) == ' ') {
                lastSpace = i;
            }
        }

        if (lastSpace == -1) { // No space found,  split at max
            result.add(line.substring(0, maxLength));
            processLine(line.substring(maxLength), maxLength, result);
        } else {
            result.add(line.substring(0, lastSpace + 1)); // Include the previous space
            processLine(line.substring(lastSpace + 1), maxLength, result);
        }
    }


}
