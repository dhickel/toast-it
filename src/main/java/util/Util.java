package util;

import enums.EntryType;
import kawa.lib.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class Util {

    public static LocalDateTime parseDateTime(List<String> dateTimeComponents) {
        if (dateTimeComponents == null || dateTimeComponents.size() != 5) {
            throw new IllegalArgumentException("Invalid date time components");
        }

        String month = dateTimeComponents.get(0);
        String day = dateTimeComponents.get(1);
        String year = dateTimeComponents.get(2);
        String hour = dateTimeComponents.get(3);
        String minute = dateTimeComponents.get(4);

        String yearFormat = year.length() == 2 ? "yy" : "yyyy";

        String dateTimeString = String.format("%s-%s-%s %s:%s", month, day, year, hour, minute);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-" + yearFormat + " HH:mm");

        return LocalDateTime.parse(dateTimeString, formatter);
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




}
