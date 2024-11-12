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

    // Try parsing the birthday using different date formats
    public static String parseDateStringInDifferentFormats(String dateString) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int cutoffYear = currentYear - 80; // Adjust this value as needed

        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                LocalDate parsedDate = LocalDate.parse(dateString, formatter);

                log.info("Birthday after parsing and adjustment: {}", parsedDate);
                return parsedDate.format(DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException e) {
                // Continue trying with other formats
                continue;
            }
        }
            // If none of the formats worked, log and return null or some default value
            log.error("Invalid birthday format: {}", dateString);
            return null;  // Could be a fallback or an error handling strategy
    }



    public static String formatBirthDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public static LocalDate convertDateStringToLocalDate(String dateString){
        if(null!=dateString)
            return LocalDate.parse(dateString);
        else
            return null;
    }

    public static LocalDate convertDateStringToFormattedLocalDate(String dateString){
        String dateFormattedString = parseDateStringInDifferentFormats(dateString);
        if(null!=dateFormattedString)
            return LocalDate.parse(dateFormattedString);
        else
            return null;
    }

}
