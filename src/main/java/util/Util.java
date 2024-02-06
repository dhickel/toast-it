package util;

import enums.EntryType;
import enums.Month;
import enums.Year;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
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

    public static void createYearMonthStructure(String rootPath) throws IOException {
        int currentYear = LocalDate.now().getYear();
        Path yearPath = Paths.get(rootPath, String.valueOf(currentYear));

        // Create the year directory if it doesn't exist
        if (!Files.exists(yearPath)) {
            Files.createDirectories(yearPath);
        }

        // Create directories for each month
        for (int month = 1; month <= 12; month++) {
            LocalDate date = LocalDate.of(currentYear, month, 1);
            String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            Path monthPath = yearPath.resolve(monthName);

            if (!Files.exists(monthPath)) {
                Files.createDirectory(monthPath);
            }
        }
    }

    public static Path getEntriesPath(EntryType entryType, Year year, Month month) {
        return Paths.get(Settings.GET().ROOT_PATH, entryType.name(), year.name().replace("_", ""), month.name());
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


}
