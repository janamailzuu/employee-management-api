package com.sas.hr.employee_management_api.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DateUtil {

    // List of possible date formats
    private static final List<DateTimeFormatter> dateFormatters = Arrays.asList(
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM/d/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            new DateTimeFormatterBuilder()
                    .appendPattern("d-MMM-")
                    .appendValueReduced(ChronoField.YEAR, 2, 2, LocalDate.now().minusYears(100))
                    .toFormatter(),
            new DateTimeFormatterBuilder()
                    .appendPattern("dd-MMM-")
                    .appendValueReduced(ChronoField.YEAR, 2, 2, LocalDate.now().minusYears(100))
                    .toFormatter()
    );

    /**
     * Parses a date string in multiple possible formats and returns the date in ISO format (yyyy-MM-dd).
     * This method attempts to parse the given date string using a series of predefined date formatters.
     * If the date string is successfully parsed, it is returned in ISO 8601 format. If none of the formatters
     * can successfully parse the string, an error is logged and {@code null} is returned.
     *
     * The method also includes a check for dates within a reasonable range of ages (using the current year
     * and a cutoff of 80 years ago), which can be useful for validating certain types of date inputs like birthdays.
     *
     * @param dateString The date string to be parsed. This string can be in any of the supported date formats.
     * @return The parsed date in ISO 8601 format (yyyy-MM-dd) if successful, or {@code null} if parsing fails.
     */
    public static String parseDateStringInDifferentFormats(String dateString) {
        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                LocalDate parsedDate = LocalDate.parse(dateString, formatter);
                return parsedDate.format(DateTimeFormatter.ISO_DATE);
                 }catch (DateTimeParseException e) {
                    continue;
                 }
        }
            log.error("Invalid birthday format: {}", dateString);
            return null;
    }

    public static String formatBirthDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public static LocalDate convertDateStringToFormattedLocalDate(String dateString){
        String dateFormattedString = parseDateStringInDifferentFormats(dateString);
        if(null!=dateFormattedString)
            return LocalDate.parse(dateFormattedString);
        else
            return null;
    }

}
