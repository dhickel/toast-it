package io.mindspice.toastit.util;


import io.mindspice.toastit.enums.EntryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class Util {
    public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static Path getEntriesPath(EntryType entryType) throws IOException {
        var dateTime = LocalDateTime.now();
        var path = Paths.get(
                Settings.ROOT_PATH, entryType.name(),
                String.valueOf(dateTime.getYear()),
                dateTime.getMonth().toString()
        );
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        return path;
    }


    public static String toPercentage(double value) {
        return String.format("%.2f%%", value * 100);
    }

    public static String[] splitRemoveFirst(String string) {
        String[] split = string.split(" ");
        return Arrays.copyOfRange(split, 1, split.length);
    }

    public static <T extends Enum<T>> T fuzzyMatchEnum(T[] enumerations, String matchString) {
        Optional<T> bestMatch = Arrays.stream(enumerations)
                .max(Comparator.comparingInt(e -> fuzzyMatchLength(e.name(), matchString.toUpperCase())));
        return bestMatch.orElse(null);
    }

    public static <T extends Enum<T>> T enumMatch(T[] enumerations, String matchString) {
        String matchUpper = matchString.toUpperCase();
        Optional<T> bestMatch = Arrays.stream(enumerations)
                .filter(e -> e.name().startsWith(matchUpper))
                .findFirst();

        return bestMatch.orElse(null);
    }

    public static String fuzzyMatchString(List<String> strings, String matchString) {
        Optional<String> bestMatch = strings.stream()
                .max(Comparator.comparingInt(s -> fuzzyMatchLength(s.toLowerCase(), matchString.toLowerCase())));
        return bestMatch.orElse(null);
    }

    public static boolean isInt(String s) {
        return s.matches("^\\d+$");

    }

    public static int fuzzyMatchLength(String word, String matchTarget) {
        int longestMatch = 0;
        for (int i = 0; i < matchTarget.length(); i++) {
            for (int j = i + 1; j <= matchTarget.length(); j++) {
                String sub = matchTarget.substring(i, j);
                if (word.contains(sub) && sub.length() > longestMatch) {
                    longestMatch = sub.length();
                }
            }
        }
        return longestMatch;
    }
}



