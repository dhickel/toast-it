package util;

import enums.EntryType;
import kawa.lib.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class Util {
    private static DateTimeFormatter dateInputFormatter;
    public static DateTimeFormatter[] timeFormatters = {
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("h:mm a"),
            DateTimeFormatter.ofPattern("h:mma"),
    };

    public static DateTimeFormatter dateInputFormatter() {
        if (dateInputFormatter == null) {
            dateInputFormatter = DateTimeFormatter.ofPattern(Settings.DATE_INPUT_FORMAT);
        }
        return dateInputFormatter;
    }

    public static LocalDate parseDateInput(String date) throws DateTimeParseException {
        return LocalDate.parse(date, dateInputFormatter());
    }

    public static LocalTime parseTimeInput(String time) throws DateTimeException {
        for (var formatter : timeFormatters) {
            try {
                return LocalTime.parse(time, formatter).truncatedTo(ChronoUnit.MINUTES);
            } catch (DateTimeParseException e) {
                //Ignore, hackish way to parse multiple inputs FIXME regex first?
            }
        }
        throw new DateTimeException("Invalid formatting");
    }

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

    public static DateTimeFormatter dateTimeFormat() {
        return DateTimeFormatter.ofPattern("EEEE MMM dd @ HH:mm");
    }

    public static String formatNotification(String name, LocalDateTime dateTime) {
        return String.format("%s |  %s", name, dateTime.format(dateTimeFormat()));
    }

    public static String toPercentage(double value) {
        return String.format("%.2f%%", value * 100);
    }

    public static String[] splitRemoveFirst(String string) {
        String[] split = string.split(" ");
        return Arrays.copyOfRange(split, 1, split.length);
    }

    public static LocalDateTime unixToLocal(long unixTime) {
        Instant inst = Instant.ofEpochSecond(unixTime);
        return LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES);
    }

    public static long localToUnix(LocalDateTime dataTime) {
        return dataTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public static long delayToDateTime(LocalDateTime dateTime) {
        return Duration.between(LocalDateTime.now(), dateTime).toSeconds();
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



