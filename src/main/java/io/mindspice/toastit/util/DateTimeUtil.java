package io.mindspice.toastit.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;


public class DateTimeUtil {

    public static List<DateTimeFormatter> timeInputFormatters;
    public static List<DateTimeFormatter> dateInputFormatters;
    public static DateTimeFormatter dateTimeFullFormatter;
    public static DateTimeFormatter dateTimeShortFormatter;
    public static DateTimeFormatter dateWithoutTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");

    public static LocalDateTime MAX = LocalDateTime.of(9999, Month.DECEMBER, 31, 0, 0, 0).truncatedTo(ChronoUnit.MINUTES);


    static {
        dateInputFormatters = Settings.DATE_INPUT_PATTERNS.stream().map(pattern ->
                new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter(Locale.US)
        ).toList();

        timeInputFormatters = Settings.TIME_INPUT_PATTERNS.stream().map(pattern ->
                new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter(Locale.US)
        ).toList();

        dateTimeShortFormatter = DateTimeFormatter.ofPattern(Settings.DATE_TIME_SHORT_PATTERN);
        dateTimeFullFormatter = DateTimeFormatter.ofPattern(Settings.DATE_TIME_FULL_PATTERN);

    }


    public static LocalDate parseDateInput(String date) throws DateTimeException {
        for (var formatter : dateInputFormatters) {
            try {
                return LocalDate.parse(date, formatter);
            } catch (DateTimeParseException e) {
                //Ignore, hackish way to parse multiple inputs FIXME regex first?
            }
        }
        throw new DateTimeException("Invalid formatting");
    }

    public static LocalTime parseTimeInput(String time) throws DateTimeException {
        for (var formatter : timeInputFormatters) {
            try {
                return LocalTime.parse(time, formatter).truncatedTo(ChronoUnit.MINUTES);
            } catch (DateTimeParseException e) {
                //Ignore, hackish way to parse multiple inputs FIXME regex first?
            }
        }
        throw new DateTimeException("Invalid formatting");
    }

    public static String printDateTimeShort(LocalDateTime dateTime) {
        return dateTimeShortFormatter.format(dateTime);
    }

    public static String printDateTimeFull(LocalDateTime dateTime) {
        return dateTimeFullFormatter.format(dateTime);
    }

    public static String printDateWithoutTime(LocalDateTime dateTime) {
        return dateWithoutTimeFormatter.format(dateTime);
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
