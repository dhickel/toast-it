package io.mindspice.toastit.util;

import io.mindspice.toastit.entries.Entry;
import io.mindspice.toastit.enums.EntryType;
import org.jline.builtins.Nano;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;

import java.util.*;
import java.util.function.Consumer;


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

    public static String removeFirstWord(String s) {
        return s.substring(s.indexOf(" ") + 1);
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
        return bestMatch;
    }

    public static <T extends Entry> T entryMatch(List<T> entries, String matchString) {
        String matchLower = matchString.toLowerCase().trim();

        List<T> matches = entries.stream()
                .filter(e -> e.name().trim().toLowerCase().startsWith(matchLower))
                .toList();

        T bestMatch = null;
        int mostMatch = 0;

        for (T match : matches) {
            String matchName = match.name().trim().toLowerCase();
            int itrLen = Math.min(matchLower.length(), matchName.length());
            int count = 0;
            while (count < itrLen && matchLower.charAt(count) == matchName.charAt(count)) {
                ++count;
            }
            if (count > mostMatch) {
                mostMatch = count;
                bestMatch = match;
            }
        }
        return bestMatch;
    }

    public static String stringMatch(List<String> entries, String matchString) {

        String matchLower = matchString.toLowerCase().trim();

        List<String> matches = entries.stream()
                .filter(e -> e.trim().toLowerCase().startsWith(matchLower))
                .toList();

        String bestMatch = null;
        int mostMatch = 0;

        for (String match : matches) {
            String matchName = match.trim().toLowerCase();
            int itrLen = Math.min(matchLower.length(), matchName.length());
            int count = 0;
            while (count < itrLen && matchLower.charAt(count) == matchName.charAt(count)) {
                ++count;
            }
            if (count > mostMatch) {
                mostMatch = count;
                bestMatch = match;
            }
        }
        return bestMatch;
    }

    public static boolean isInt(String s) {
        return s.matches("^\\d+$");
    }

    public static Consumer<Path> nanoInstance(Terminal terminal) {
        return (Path file) -> {
            try {
                Nano nano = new Nano(terminal, file);
                nano.tabs = 2;
                nano.matchBrackets = "(<[{)>]}";
                nano.brackets = "\"’)>]}";
                nano.mouseSupport = true;
                nano.open(file.toFile().getAbsolutePath());
                nano.run();
                nano.setRestricted(true);
            } catch (Exception e) {
                System.err.println("Error launching nano: " + e.getMessage());
            }
        };
    }

    public static String tempEdit(Consumer<Path> consumer, String existingData) throws IOException {

        Path tempPath = Paths.get(Settings.TEMP_PATH);
        String tempId = UUID.randomUUID().toString();

        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }
        Path tempFile = Files.createTempFile(tempPath, tempId, ".temp");
        Files.write(tempFile, existingData.getBytes());
        consumer.accept(tempFile);

        String newData = Files.readString(tempFile);
        Files.delete(tempFile);

        return newData;


    }


}



