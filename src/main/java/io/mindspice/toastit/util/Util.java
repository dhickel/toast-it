package io.mindspice.toastit.util;


import io.mindspice.toastit.enums.EntryType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;

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


    public static <T extends Enum<T>> T enumMatch(T[] enumerations, String matchString) {
        String matchUpper = matchString.toUpperCase();
        List<T> matches = Arrays.stream(enumerations)
                .filter(e -> e.name().startsWith(matchUpper))
                .toList();

        T bestMatch = null;
        int mostMatch = 0;

        for (T match : matches) {
            String enumName = match.name();
            int itrLen = Math.min(matchUpper.length(), enumName.length());
            int count = 0;
            while (count < itrLen && matchUpper.charAt(count) == enumName.charAt(count)) {
                ++count;
            }
            if (count > mostMatch) {
                mostMatch = count;
                bestMatch = match;
            }
        }
        return  bestMatch;
    }

    public static boolean isInt(String s) {
        return s.matches("^\\d+$");

    }


}



